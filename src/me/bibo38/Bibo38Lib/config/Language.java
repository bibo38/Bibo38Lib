package me.bibo38.Bibo38Lib.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Language
{
	private String lang;
	private JavaPlugin main;
	private File folder;
	private HashSet<String> filelangs; // Die Sprachteile, welche in extra dateien untergebracht sind
	
	private CustomConfig langCfg = null;
	private Language father = null;
	
	/**
	 * Konstruktor zum erstellen einer Sprache
	 * 
	 * @param elang Die Sprachdatei als yml im lang Ordner (z.b. "de" -> lang/de.yml)
	 * @param emain Das JavaPlugin Objekt
	 */
	public Language(String lang, JavaPlugin main, String... filelangs)
	{
		this.main = main;
		folder = new File(main.getDataFolder(), "lang");
		this.filelangs = new HashSet<String>(Arrays.asList(filelangs));
		
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		
		this.setLang(lang);
	}
	
	private Language(Language father, CustomConfig newCfg)
	{
		this.father = father;
		this.main = father.main;
		filelangs = new HashSet<String>();
		langCfg = newCfg;
	}
	
	/**
	 * Die Sprache setzen
	 * 
	 * @param elang Die Sprachdatei als yml im lang Ordner (z.b. "de" -> lang/de.yml)
	 */
	public void setLang(String lang)
	{
		if(father != null)
		{
			father.setLang(lang);
			return;
		}
		
		this.lang = lang;
		langCfg = new CustomConfig(new File(folder, lang + ".yml"), lang + ".yml", main);
		for(String akt : filelangs)
		{
			File f = new File(folder, lang+"-"+akt+".txt");
			if(f.exists())
				continue;
			try
			{
				f.createNewFile();
				InputStream is = main.getResource(lang+"-"+akt+".txt");
				if(is != null)
				{
					OutputStream os = new FileOutputStream(f);
					byte[] buffer = new byte[1024];
					int read;
					while((read = is.read(buffer)) >= 0)
						os.write(buffer, 0, read);
					os.close();
					is.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Einen speziellen Text aus der Sprachdatei heraussuchen
	 * 
	 * @param key Der Name des Textes
	 * @param args Die Argumente f�r den String zu ersetzen
	 * @return Der Text
	 */
	public String getText(String key, String... args)
	{
		String ret = "";
		if(filelangs.contains(key))
		{
			// ret aus datei holen
			Path f = Paths.get(folder.getAbsolutePath(), lang+"-"+key+".txt");
			if(f.toFile().exists())
			{
				try
				{
					byte[] encoded = Files.readAllBytes(f);
					ret = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		} else
			ret = langCfg.getString(key);
		
		for(int i = 0; i < args.length; i++)
			ret = ret.replaceFirst(Pattern.quote("$$"), args[i]).replace("$"+(i+1)+"$", args[i]);
		
		return ret.replace('&', ChatColor.COLOR_CHAR).replace("\r", "");
	}
	
	public CustomConfig getCfg()
	{
		return langCfg;
	}
	
	public void sendText(CommandSender p, String key, boolean error, String... args)
	{
		this.sendText(p, key, error? ChatColor.RED : ChatColor.GREEN, args);
	}
	
	public void sendText(CommandSender p, String key, String... args)
	{
		this.sendText(p, key, ChatColor.WHITE, args);
	}
	
	public void sendText(CommandSender p, String key, ChatColor col, String... args)
	{
		String text[] = this.getText(key, args).split("\n");
		for(String t : text)
			p.sendMessage(col+t);
	}
	
	public boolean existKey(String key, boolean acceptDefault)
	{
		return langCfg.contains(key, acceptDefault);
	}
	
	public Language getSection(String section)
	{
		return new Language(this, langCfg.getSection(section));
	}
	
	/**
	 * Funktion um die aktuelle Sprache zu ermitteln
	 * 
	 * @return Die Sprache
	 */
	public String getLang()
	{
		if(father != null)
			return father.getLang();
		return lang;
	}
}
