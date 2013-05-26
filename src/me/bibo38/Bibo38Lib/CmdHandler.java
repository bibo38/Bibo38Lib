package me.bibo38.Bibo38Lib;

import org.bukkit.command.CommandSender;

import me.bibo38.Bibo38Lib.command.ACommand;
import me.bibo38.Bibo38Lib.command.Command;
import me.bibo38.Bibo38Lib.command.CommandListener;

public class CmdHandler extends Startfunc implements CommandListener
{
	@ACommand(maxArgs = 0, description = "Reloads the Config and Language", permissions = "reload")
	public void reload(CommandSender cs, String args[])
	{
		main.reloadConfig();
		main.lang.setLang(main.getConfig().getString("lang"));
		Command.updateColor();
	}
}
