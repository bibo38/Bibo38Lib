package me.bibo38.Bibo38Lib;

import java.util.TreeMap;

import me.bibo38.Bibo38Lib.command.Command;
import me.bibo38.Bibo38Lib.command.CommandHandler;
import me.bibo38.Bibo38Lib.command.CommandListener;
import me.bibo38.Bibo38Lib.command.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class CmdHandler extends Startfunc implements CommandListener
{
	@Command(description = "Reloads the Config and Language", permissions = "reload")
	public void reload()
	{
		main.reloadConfig();
		FileConfiguration cfg = main.getConfig();
		main.lang.setLang(cfg.getString("lang"));
		CommandHandler.updateColor();
		
		ConfigurationSection jdbc = cfg.getConfigurationSection("database");
		main.jdbcURL = jdbc.getString("url");
		main.jdbcUser = jdbc.getString("user");
		main.jdbcPass = jdbc.getString("pass");
	}
	
	@Command(description = "Gets the UUID of a Player", usage = "[Name]", permissions = "uuid")
	public void uuid(CommandSender cs, @Optional Player... other)
	{
		TreeMap<String, Player> toShow = new TreeMap<String, Player>();
		
		if(other != null)
		{
			for(Player p : other)
				toShow.put(p.getName(), p);
		} else if(cs instanceof Player)
			toShow.put(((Player) cs).getName(), (Player) cs);
		else
			main.lang.sendText(cs, "beplayer", true);
		
		for(Player p : toShow.values())
			cs.sendMessage(p.getName() + ": " + p.getUniqueId().toString());
	}
	
	@Command(description = "Gives a skull of a player", usage = "[Name]", permissions = "skull")
	public void skull(Player p, @Optional String p2)
	{
		if(p2 == null)
			p2 = p.getName();
		
		ItemStack skull = new ItemStack(Material.SKULL_ITEM);
		Utils.setSkullName(skull, p2);
		p.getInventory().addItem(skull);
	}
	
	@Command(description = "Disables a Plugin", usage = "[Name]", permissions = "disable")
	public void disable(String plugin)
	{
		PluginManager pm = Bukkit.getPluginManager();
		Plugin plug = pm.getPlugin(plugin);
		if(plug != null && plug.isEnabled())
			pm.disablePlugin(plug);
	}
	
	@Command(description = "Enables a Plugin", usage = "[Name]", permissions = "enable")
	public void enable(String plugin)
	{
		PluginManager pm = Bukkit.getPluginManager();
		Plugin plug = pm.getPlugin(plugin);
		if(plug != null && !plug.isEnabled())
			pm.enablePlugin(plug);
	}
}
