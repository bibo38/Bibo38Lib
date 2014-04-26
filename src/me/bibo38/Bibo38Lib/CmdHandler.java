package me.bibo38.Bibo38Lib;

import me.bibo38.Bibo38Lib.command.Command;
import me.bibo38.Bibo38Lib.command.CommandHandler;
import me.bibo38.Bibo38Lib.command.CommandListener;
import me.bibo38.Bibo38Lib.command.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

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
	public void uuid(CommandSender cs, @Optional Player other)
	{
		Player p = null;
		if(other != null)
			p = other;
		else if(cs instanceof Player)
			p = (Player) cs;
		else
			main.lang.sendText(cs, "beplayer", true);
		
		if(p != null)
			cs.sendMessage(p.getUniqueId().toString());
	}
	
	@Command(description = "Gives a skull of a player", usage = "[Name]", permissions = "skull")
	public void skull(Player p, @Optional Player p2)
	{
		if(p2 == null)
			p2 = p;
		
		BlockIterator it = new BlockIterator(p);
		Block block;
		while((block = it.next()).getType() == Material.AIR);
		if(block != null && block.getType() == Material.SKULL)
			Utils.setSkullName(block.getLocation(), p2);
		else
		{
			ItemStack skull = new ItemStack(Material.SKULL_ITEM);
			Utils.setSkullName(skull, p2.getName());
			p.getInventory().addItem(skull);
		}
	}
}
