package me.bibo38.Bibo38Lib;

import me.bibo38.Bibo38Lib.command.ACommand;
import me.bibo38.Bibo38Lib.command.Command;
import me.bibo38.Bibo38Lib.command.CommandListener;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class CmdHandler extends Startfunc implements CommandListener
{
	@ACommand(maxArgs = 0, description = "Reloads the Config and Language", permissions = "reload")
	public void reload(CommandSender cs, String args[])
	{
		main.reloadConfig();
		FileConfiguration cfg = main.getConfig();
		main.lang.setLang(cfg.getString("lang"));
		Command.updateColor();
		
		ConfigurationSection jdbc = cfg.getConfigurationSection("database");
		main.jdbcURL = jdbc.getString("url");
		main.jdbcUser = jdbc.getString("user");
		main.jdbcPass = jdbc.getString("pass");
	}
}
