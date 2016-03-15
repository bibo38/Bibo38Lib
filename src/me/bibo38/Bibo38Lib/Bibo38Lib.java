package me.bibo38.Bibo38Lib;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import me.bibo38.Bibo38Lib.command.CommandHandler;
import me.bibo38.Bibo38Lib.config.Language;
import me.bibo38.Bibo38Lib.spout.WebServer;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Bibo38Lib extends JavaPlugin
{
	public boolean vaultOn = false;
	public Language lang;
	
	public String jdbcURL;
	public String jdbcUser;
	public String jdbcPass;
	
	private PluginDescriptionFile pdFile;
	private Logger log;
	
	private WebServer webServ = null;
	
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
		
		// Spout wird zurückgestellt, da es noch nicht kompatibel mit der aktuellen bukkit-Version ist
		// Falls Spout aktiviert ist den Spout-Teil aktivieren
		/* if(this.getServer().getPluginManager().getPlugin("Spout") != null || true)
		{
			log.info("Activate Spout things :-)");
			try
			{
				mySpout = new Spout(new File(this.getDataFolder(), "cache"));
			} catch (IOException e)
			{
				log.info("Failed activating Spout things");
			}
		} */
		try
		{
			webServ = new WebServer(this.getConfig().getInt("webport"), new File(this.getDataFolder(), "cache"));
		} catch (IOException e)
		{
			log.info("Error starting WebServer");
			e.printStackTrace();
		}
		
		log.info("Bibo38Lib Version " + pdFile.getVersion() + " by bibo38 was activated!");
	}
	
	public WebServer getWebServer()
	{
		return webServ;
	}
	
	@Override
	public void onDisable()
	{
		log.info("Stopping all running WebServers");
		WebServer.stopAll();
		
		log.info("Bibo38Lib Version " + pdFile.getVersion() + " by bibo38 was deactivated!");
	}
	
	public boolean isVaultActivated()
	{
		return vaultOn;
	}
}
