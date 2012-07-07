package me.bibo38.Bibo38Lib;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import me.bibo38.Bibo38Lib.spout.Spout;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Bibo38Lib extends JavaPlugin
{
	private PluginDescriptionFile pdFile;
	private Logger log;
	
	public boolean vaultOn = false;
	
	private Spout mySpout = null;
	
	@Override
	public void onEnable()
	{
		log = this.getLogger();
		pdFile = this.getDescription();
		// Die Main Klassen setzen
		Startfunc.main = this;
				
		// Nach Vault prüfen
		if(this.getServer().getPluginManager().getPlugin("Vault") == null)
		{
			vaultOn = false;
		} else
		{
			vaultOn = true;
		}
		
		// Ordner erstellen
		if(!(new File(this.getDataFolder(), "cache")).exists())
		{
			new File(this.getDataFolder(), "cache").mkdirs();
		}
		
		// Falls Spout aktiviert ist den Spout-Teil aktivieren
		if(this.getServer().getPluginManager().getPlugin("Spout") != null)
		{
			log.info("Activate Spout things :-)");
			try
			{
				mySpout = new Spout(new File(this.getDataFolder(), "cache"));
			} catch (IOException e)
			{
				log.info("Failed activating Spout things");
			}
		}
		
		log.info("Bibo38Lib Version " + pdFile.getVersion() + " by bibo38 was activated!");
	}
	
	@Override
	public void onDisable()
	{
		log.info("Bibo38Lib Version " + pdFile.getVersion() + " by bibo38 was deactivated!");
	}
	
	public boolean isVaultActivated()
	{
		return vaultOn;
	}
	
	public Spout getSpout()
	{
		return mySpout;
	}
}
