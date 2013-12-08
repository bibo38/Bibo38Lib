package me.bibo38.Bibo38Lib.game;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import me.bibo38.Bibo38Lib.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

public class Arena implements Runnable, Listener
{
	protected ConcurrentHashMap<Player, Integer> players = new ConcurrentHashMap<Player, Integer>();
	protected HashMap<Integer, Location> spawns = new HashMap<Integer, Location>();
	protected Location lobby = null, finish;
	protected JavaPlugin main;
	protected Scoreboard score = null;
	protected boolean started = false;
	
	public Arena(JavaPlugin main)
	{
		this.main = main;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public void save(ConfigurationSection cfs)
	{
		Utils.saveLocation(cfs.createSection("lobby"), lobby);
		Utils.saveLocation(cfs.createSection("finish"), finish);
		cfs = cfs.createSection("spawns");
		for(Entry<Integer, Location> akt : spawns.entrySet())
			Utils.saveLocation(cfs.createSection(akt.getKey()+""), akt.getValue());
	}
	
	public void load(ConfigurationSection cfs)
	{
		spawns.clear();
		lobby = Utils.getLocation(cfs, "lobby");
		finish = Utils.getLocation(cfs, "finish");
		cfs = cfs.getConfigurationSection("spawns");
		if(cfs != null)
			for(String s : cfs.getKeys(false))
				spawns.put(Integer.parseInt(s), Utils.getLocation(cfs, s));
	}
	
	public void setLobby(Location l)
	{
		lobby = l;
		finish = l.getWorld().getSpawnLocation();
	}
	
	public Location getLobby()
	{
		return lobby;
	}
	
	public void setSpawn(Integer team, Location l)
	{
		if(l == null)
			spawns.remove(team);
		else
			spawns.put(team, l);
	}
	
	public boolean isInside(Player p)
	{
		return players.containsKey(p);
	}
	
	public void start()
	{
		this.start(0);
	}
	
	public boolean hasStarted()
	{
		return started;
	}
	
	public void start(int delay)
	{
		if(delay > 0)
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, this, 20L*delay);
		else
			this.run();
	}
	
	public void stop()
	{
		started = false;
		for(Player p : players.keySet())
			this.leave(p);
	}
	
	public void delete()
	{
		Bukkit.getServicesManager().unregister(this);
		if(started)
			stop();
	}
	
	public void join(Player p)
	{
		p.teleport(lobby);
		players.put(p, -1);
		
		// Heal Player
		p.setHealth(20D);
		p.setExhaustion(0F);
		p.setSaturation(20F);
		p.setGameMode(GameMode.SURVIVAL);
		
		if(score != null)
			p.setScoreboard(score);
	}
	
	public void leave(Player p)
	{
		if(players.remove(p) == null)
			return;
		p.teleport(finish);
		if(score != null)
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}
	
	public boolean setTeam(Player p, int team)
	{
		if(!players.containsKey(p))
			return false;
		
		players.put(p, team);
		return true;
	}

	@Override
	public void run()
	{
		started = true;
		for(Entry<Player, Integer> akt : players.entrySet())
		{
			if(!spawns.containsKey(akt.getValue()))
				akt.getKey().teleport(finish);
			akt.getKey().teleport(spawns.get(akt.getValue()));
		}
	}
	
	protected Location getSpawn(Player p)
	{
		return spawns.get(players.get(p));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		if(players.containsKey(e.getPlayer()))
		{
			if(started)
			{
				Location spawn = this.getSpawn(e.getPlayer());
				if(spawn != null)
					e.setRespawnLocation(spawn);
			} else if(lobby != null)
				e.setRespawnLocation(lobby);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		leave(e.getPlayer());
	}
}
