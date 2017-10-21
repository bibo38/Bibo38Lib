package me.bibo38.Bibo38Lib.config;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

public final class FileChangeConfigReloader implements Listener
{
	private Path file;
	private WatchService service;
	private Plugin plugin;
	private Thread curThread; // null means cancel the thread
	private BukkitTask doReloadTask;

	private FileChangeConfigReloader(Path file, WatchService service, Plugin plugin)
	{
		this.file = file;
		this.service = service;
		this.plugin = plugin;

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::watchFile);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@SuppressWarnings("unchecked")
	private void watchFile()
	{
		curThread = Thread.currentThread();

		WatchKey key;
		while(true)
		{
			if(curThread == null)
				return;
			try
			{
				key = service.take();
			} catch (InterruptedException e)
			{
				if(curThread == null)
					return;
				else
					continue;
			}

			for(WatchEvent<?> e : key.pollEvents())
			{
				if(e.kind() == StandardWatchEventKinds.OVERFLOW) // Can happen even if we don't register
					continue;

				Path changedFile = ((WatchEvent<Path>) e).context();
				if(!changedFile.equals(file))
					continue;

				if(doReloadTask == null)
					doReloadTask = plugin.getServer().getScheduler().runTask(plugin, this::doReload);
			}

			if(!key.reset())
				return; // Directory inaccessible
		}
	}

	/**
	 * Used to synchronize and prevent multiple fast reloads from multiple file change events.
	 */
	private void doReload()
	{
		plugin.reloadConfig();
		doReloadTask = null;
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent e)
	{
		if(!e.getPlugin().equals(plugin))
			return;

		Thread toCancel = curThread;
		curThread = null; // Exit condition
		toCancel.interrupt();
	}

	public static boolean register(Plugin plugin, Path file)
	{
		try
		{
			WatchService service = FileSystems.getDefault().newWatchService();
			file.getParent().register(service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
			new FileChangeConfigReloader(file.getFileName(), service, Objects.requireNonNull(plugin));
		} catch (IOException e)
		{
			return false;
		}
		return true;
	}
}
