package me.bibo38.Bibo38Lib.config;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomConfig
{
	private FileConfiguration customCfg = null;
	private File customCfgFile = null;
	private JavaPlugin main;
	private String cfgDefaultName = null;
	
	private Logger log;
	
	private void construct(File file, JavaPlugin emain)
	{
		customCfgFile = file;
		main = emain;
		log = main.getLogger();
		
		this.reload();
		customCfg.options().copyDefaults(true);
		this.save();
	}
	
	/**
	 * Konstruktor zum Erstellen einer eigenen Konfiguration
	 * 
	 * @param file Die Datei
	 * @param cfgName Der Dateiname als String (als Resource)
	 * @param emain Das JavaPlugin
	 */
	public CustomConfig(File file, String cfgName, JavaPlugin emain)
	{
		cfgDefaultName = cfgName;
		this.construct(file, emain);
	}
	
	/**
	 * Konstruktor zum Erstellen einer eigenen Konfiguration
	 * 
	 * @param filestr Der Dateiname im Plugin Hauptverzeichnis
	 * @param emain Das JavaPlugin
	 */
	public CustomConfig(String filestr, JavaPlugin emain)
	{
		cfgDefaultName = filestr;
		File file = new File(emain.getDataFolder(), filestr);
		this.construct(file, emain);
	}
	
	/**
	 * Die Konfiguration neu laden
	 */
	public void reload()
	{
		if(!customCfgFile.exists())
		{
			try
			{
				customCfgFile.createNewFile();
			} catch(Exception e)
			{
				log.info("Error creating the File " + customCfgFile.getAbsolutePath() + ":");
				e.printStackTrace();
				return;
			}
		}
		
		customCfg = YamlConfiguration.loadConfiguration(customCfgFile);
		
		InputStream defCfgStream = main.getResource(cfgDefaultName);
		if(defCfgStream != null)
		{
			YamlConfiguration defCfg = YamlConfiguration.loadConfiguration(defCfgStream);
			customCfg.setDefaults(defCfg);
		}
	}
	
	/**
	 * Die Konfiguration in der Datei speichern
	 */
	public void save()
	{
		if(customCfg == null || customCfgFile == null)
		{
			return;
		}
		
		try
		{
			customCfg.save(customCfgFile);
		} catch(Exception e)
		{
			log.info("Error saving the Config " + customCfgFile.getAbsolutePath() + ":");
			e.printStackTrace();
		}
	}
	
	/**
	 * Funktion um sich das Konfigurationsobjekt zurückliefern zu lassen
	 * 
	 * @return Die Konfiguration als FileConfiguration
	 */
	public FileConfiguration getCfg()
	{
		if(customCfg == null)
		{
			this.reload();
		}
		
		return customCfg;
	}
}
