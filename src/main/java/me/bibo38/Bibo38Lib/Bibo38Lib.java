package me.bibo38.Bibo38Lib;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import me.bibo38.Bibo38Lib.command.CommandHandler;
import me.bibo38.Bibo38Lib.config.Language;

public class Bibo38Lib extends JavaPlugin
{
	public boolean vaultOn = false;
	public Language lang;
	
	public String jdbcURL;
	public String jdbcUser;
	public String jdbcPass;
	
	private PluginDescriptionFile pdFile;
	private Logger log;
		
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
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		lang = new Language(this.getConfig().getString("lang"), this);
		
		CmdHandler cmd = new CmdHandler();
		new CommandHandler(this, "bibo38lib", "bibo38lib", cmd, lang);
		cmd.reload();
		
		// Ordner erstellen
		if(!(new File(this.getDataFolder(), "cache")).exists())
		{
			new File(this.getDataFolder(), "cache").mkdirs();
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
}
