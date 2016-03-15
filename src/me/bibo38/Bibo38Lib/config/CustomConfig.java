package me.bibo38.Bibo38Lib.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomConfig
{
	private ConfigurationSection confSec = null;
	private FileConfiguration customCfg = null;
	private File customCfgFile = null;
	private JavaPlugin main;
	private String cfgDefaultName = null;
	
	private Logger log;
	private CustomConfig father = null;
	
	/**
	 * Konstruktor zum Erstellen einer eigenen Konfiguration
	 * 
	 * @param file Die Datei
	 * @param cfgName Der Dateiname als String (als Resource)
	 * @param emain Das JavaPlugin
	 */
	public CustomConfig(File file, String cfgName, JavaPlugin main)
	{
		cfgDefaultName = cfgName;
		this.construct(file, main);
	}
	
	/**
	 * Konstruktor zum Erstellen einer eigenen Konfiguration
	 * 
	 * @param filestr Der Dateiname im Plugin Hauptverzeichnis
	 * @param emain Das JavaPlugin
	 */
	public CustomConfig(String filestr, JavaPlugin main)
	{
		cfgDefaultName = filestr;
		File file = new File(main.getDataFolder(), filestr);
		this.construct(file, main);
	}
	
	private CustomConfig(CustomConfig old, ConfigurationSection sec)
	{
		father = old;
		confSec = sec;
		main = old.main;
		log = old.log;
	}
	
	private void construct(File file, JavaPlugin main)
	{
		customCfgFile = file;
		this.main = main;
		log = main.getLogger();
		
		this.reload();
		customCfg.options().copyDefaults(true);
		this.save();
	}
	
	/**
	 * Die Konfiguration neu laden
	 */
	public void reload()
	{
		if(father != null)
		{
			father.reload();
			return;
		}
		
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
			YamlConfiguration defCfg = YamlConfiguration.loadConfiguration(new InputStreamReader(defCfgStream));
			customCfg.setDefaults(defCfg);
		}
	}
	
	/**
	 * Die Konfiguration in der Datei speichern
	 */
	public void save()
	{
		if(father != null)
			father.save();
		if(customCfg == null || customCfgFile == null || father != null)
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
	
	public CustomConfig getSection(String section)
	{
		ConfigurationSection neu;
		if(father == null)
		{
			neu = customCfg.getConfigurationSection(section);
			if(neu == null)
				neu = customCfg.createSection(section);
		} else
		{
			neu = confSec.getConfigurationSection(section);
			if(neu == null)
				neu = confSec.createSection(section);
		}
		return new CustomConfig(this, neu);
	}
	
	/**
	 * Funktion um sich das Konfigurationsobjekt zurückliefern zu lassen
	 * 
	 * @return Die Konfiguration als FileConfiguration
	 */
	public FileConfiguration getCfg()
	{
		if(customCfg == null)
			this.reload();
		
		return customCfg;
	}
	
	public boolean contains(String path, boolean acceptDefault)
	{
		if(father == null)
		{
			if(acceptDefault)
				return customCfg.contains(path);
			else
				return customCfg.get(path, null) != null;
		} else
		{
			if(acceptDefault)
				return confSec.contains(path);
			else
				return confSec.get(path, null) != null;
		}
	}
	
	// ---
	
	public void set(String path, Object value)
	{
		if(father == null)
			customCfg.set(path, value);
		else
			confSec.set(path, value);
	}
	
	public double getDouble(String path)
	{
		return (father == null)? customCfg.getDouble(path) : confSec.getDouble(path);
	}
	
	public int getInt(String path)
	{
		return (father == null)? customCfg.getInt(path) : confSec.getInt(path);
	}
	
	public long getLong(String path)
	{
		return (father == null)? customCfg.getLong(path) : confSec.getLong(path);
	}
	
	public String getString(String path)
	{
		return (father == null)? customCfg.getString(path) : confSec.getString(path);
	}
	
	public ItemStack getItemStack(String path)
	{
		return (father == null)? customCfg.getItemStack(path) : confSec.getItemStack(path);
	}
	
	// -----
	
	public List<Double> getDoubleList(String path)
	{
		return (father == null)? customCfg.getDoubleList(path) : confSec.getDoubleList(path);
	}
	
	public List<Float> getFloatList(String path)
	{
		return (father == null)? customCfg.getFloatList(path) : confSec.getFloatList(path);
	}
	
	public List<Integer> getIntegerList(String path)
	{
		return (father == null)? customCfg.getIntegerList(path) : confSec.getIntegerList(path);
	}
	
	public List<Long> getLongList(String path)
	{
		return (father == null)? customCfg.getLongList(path) : confSec.getLongList(path);
	}
	
	public List<String> getStringList(String path)
	{
		return (father == null)? customCfg.getStringList(path) : confSec.getStringList(path);
	}
}
