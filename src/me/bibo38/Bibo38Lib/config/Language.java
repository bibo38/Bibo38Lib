package me.bibo38.Bibo38Lib.config;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

public class Language
{
	private String lang;
	private JavaPlugin main;
	private File folder;
	
	private CustomConfig langCfg = null;
	
	/**
	 * Konstruktor zum erstellen einer Sprache
	 * 
	 * @param elang Die Sprachdatei als yml im lang Ordner (z.b. "de" -> lang/de.yml)
	 * @param emain Das JavaPlugin Objekt
	 */
	public Language(String elang, JavaPlugin emain)
	{
		main = emain;
		folder = new File(main.getDataFolder(), "lang");
		
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		
		this.setLang(elang);
	}
	
	/**
	 * Die Sprache setzen
	 * 
	 * @param elang Die Sprachdatei als yml im lang Ordner (z.b. "de" -> lang/de.yml)
	 */
	public void setLang(String elang)
	{
		lang = elang;
		langCfg = new CustomConfig(new File(folder, lang + ".yml"), lang + ".yml", main);
	}
	
	/**
	 * Einen speziellen Text aus der Sprachdatei heraussuchen
	 * 
	 * @param key Der Name des Textes
	 * @return Der Text
	 */
	public String getText(String key)
	{
		return langCfg.getCfg().getString(key);
	}
	
	/**
	 * Funktion um die aktuelle Sprache zu ermitteln
	 * 
	 * @return Die Sprache
	 */
	public String getLang()
	{
		return lang;
	}
}
