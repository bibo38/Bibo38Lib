package me.bibo38.Bibo38Lib;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Bibo38Lib extends JavaPlugin
{
	private PluginDescriptionFile pdFile;
	private Logger log;
	
	protected boolean vaultOn = false;
	
	@Override
	public void onEnable()
	{
		log = this.getLogger();
		pdFile = this.getDescription();
		
		// Nach Vault prüfen
		if(this.getServer().getPluginManager().getPlugin("Vault") == null)
		{
			vaultOn = false;
		} else
		{
			vaultOn = true;
		}
		
		
		// Die Main Klassen setzen
		Economy.main = this;
		Permissions.main = this;
		
		log.info("Bibo38Lib Version " + pdFile.getVersion() + " by bibo38 was activated!");
	}
	
	@Override
	public void onDisable()
	{
		log.info("Bibo38Lib Version " + pdFile.getVersion() + " by bibo38 was deactivated!");
	}
}
