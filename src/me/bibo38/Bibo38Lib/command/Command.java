package me.bibo38.Bibo38Lib.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import me.bibo38.Bibo38Lib.Permissions;
import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.config.Language;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Command extends Startfunc implements CommandExecutor, TabCompleter
{
	protected JavaPlugin plug;
	
	private String cmdName;
	private CommandListener cmdListener;
	private HashMap<String, Method> cmds;
	private static ChatColor col;
	
	private Permissions perm;
	
	/**
	 * Konstruktor um ein neues Kommando hinzuzufügen
	 * 
	 * @param emain Das JavaPlugin
	 * @param perms Der Permission-Baum
	 * @param ecmdName Der name des Kommandos
	 * @param ecmdListener Der Listener für das Kommando
	 */
	public Command(JavaPlugin emain, String perms, String ecmdName, CommandListener ecmdListener)
	{
		plug = emain;
		cmdListener = ecmdListener;
		cmdName = ecmdName;
		perm = new Permissions(perms);
		updateColor();
		
		cmds = new HashMap<String, Method>();
		cmds.clear();
		
		// Den Listener untersuchen
		for(Method m : cmdListener.getClass().getMethods())
		{
			ACommand annot = m.getAnnotation(ACommand.class);
			if(annot != null)
			{
				// Funktion auslesen
				String name = m.getName().toLowerCase();
				if(annot.umlaut())
					name = name.replace("ae", "ä").replace("oe", "ö").replace("ue", "ü");
				
				if(m.getParameterTypes().length == 2 &&
						m.getParameterTypes()[0].equals(CommandSender.class) &&
						m.getParameterTypes()[1].equals(String[].class))
				{
					cmds.put(name, m);
				}
			}
		}
		
		PluginCommand cmd = plug.getCommand(cmdName);
		if(cmd == null)
			throw new IllegalArgumentException("Not registered Command "+cmdName);
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
	}
	
	public static void updateColor()
	{
		col = ChatColor.getByChar(main.getConfig().getString("helpcolor"));
	}
	
	/**
	 * Konstruktor um ein neues Kommando hinzuzufügen
	 * 
	 * @param emain Das JavaPlugin
	 * @param ecmdName Der Name des Kommandos
	 * @param ecmdListener Der Listener für das Kommando
	 */
	public Command(JavaPlugin emain, String ecmdName, CommandListener ecmdListener)
	{
		this(emain, ecmdName.toLowerCase(), ecmdName, ecmdListener);
	}

	private boolean checkPerm(CommandSender cs, ACommand annot, boolean show)
	{
		// Permissions prüfen
		if(cs instanceof Player)
		{
			if(annot.permissions().equals("op"))
			{
				if(((Player) cs).isOp())
					return true;
				else if(show)
					cs.sendMessage(ChatColor.RED + main.lang.getText("noperm"));
			} else if(annot.permissions().equals("none"))
			{
				return true;
			} else if(perm.hasPerm(cs, annot.permissions(), show))
			{
				return true;
			}
			
			return false;
		} else
			return true;
	}
	
	private void sendHelp(CommandSender cs, int page)
	{
		// Seitenanzahl berechnen
		if(page < 1)
			page = 1;
		
		TreeMap<String, ACommand> allowedCmds = new TreeMap<String, ACommand>();
		for(String aktCmd : cmds.keySet().toArray(new String[0]))
		{
			ACommand annot = cmds.get(aktCmd).getAnnotation(ACommand.class);
			if(checkPerm(cs, annot, false))
				allowedCmds.put(aktCmd, annot);
		}
		
		int pages = allowedCmds.keySet().size() / 10 + 1;
		if(page > pages)
			page = pages;
		
		cs.sendMessage(col + main.lang.getText("help", plug.getName()+" "+plug.getDescription().getVersion(), String.valueOf(page), String.valueOf(pages)));
		cs.sendMessage("");
		
		String cmdArray[] = allowedCmds.keySet().toArray(new String[0]);
		for(int i = 10*(page-1); i < 10*page && i < cmdArray.length; i++)
		{
			String name = cmdArray[i];
			ACommand aktcmd = allowedCmds.get(name);
			
			cs.sendMessage(col + "/" + cmdName + " " + name + " - " + aktcmd.description());
		}
		
		cs.sendMessage("");
		if(page != pages)
			cs.sendMessage(col + main.lang.getText("next", "/" + cmdName + " help " + (page + 1)));
		
		cs.sendMessage(col + "----------------------------------------");
	}
	
	private void sendHelp(CommandSender cs, String command)
	{
		Method m = cmds.get(command.toLowerCase());
		if(m == null)
		{
			cs.sendMessage(ChatColor.RED + main.lang.getText("unkcmd"));
			cs.sendMessage("");
			this.sendHelp(cs, 1);
			return;
		}
		
		ACommand annot = m.getAnnotation(ACommand.class);
		cs.sendMessage(col + "------- " + command.toLowerCase() + " -------");
		cs.sendMessage("");
		
		Language l = main.lang;
		cs.sendMessage(col + l.getText("usage") + "/" + cmdName + " " + command.toLowerCase() + " " + annot.usage());
		cs.sendMessage(col + l.getText("desc") + annot.description());
		cs.sendMessage(col + l.getText("console") + (annot.playerNeed() ? "No" : "Yes"));
		
		if(annot.permissions().equals("op") || annot.permissions().equals("none"))
		{
			cs.sendMessage(col + l.getText("perm") + annot.permissions());
		} else
		{
			cs.sendMessage(col + l.getText("perm") + perm.getFather() + "." + annot.permissions());
		}
		
		cs.sendMessage("");
		cs.sendMessage(col + "-------------------");
	}
	
	/**
	 * Die onCommand Funktion für ankommende
	 * Befehle
	 */
	@Override
	public boolean onCommand(CommandSender cs,
			org.bukkit.command.Command cmd, String label, String[] args)
	{
		Language l = main.lang;
		if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) // Hilfe anzeigen
		{
			if(cmds.containsKey("help"))
			{
				try
				{
					cmds.get("help").invoke(cmdListener, cs, new String[0]);
				} catch(Exception e)
				{
					cs.sendMessage(ChatColor.RED + l.getText("error"));
					e.printStackTrace();
				}
				return true;
			}
			this.sendHelp(cs, 1);
			return true;
		}
		
		// Neue Args erstellen
		String[] newargs = Arrays.copyOfRange(args, 1, args.length);
		
		if(args[0].equalsIgnoreCase("help"))
		{
			if(cmds.containsKey("help"))
			{
				try
				{
					cmds.get("help").invoke(cmdListener, cs, newargs);
				} catch(Exception e)
				{
					cs.sendMessage(ChatColor.RED + l.getText("error"));
					e.printStackTrace();
				}
				return true;
			}
			int seite = 1;
			String command = "";
			
			try
			{
				seite = Integer.parseInt(args[1]);
			} catch(NumberFormatException e)
			{
				command = args[1];
			}
			
			if(command.equals(""))
			{
				this.sendHelp(cs, seite);
			} else
			{
				this.sendHelp(cs, command);
			}
			
			return true;
		}
		
		if(!cmds.containsKey(args[0].toLowerCase()))
		{
			cs.sendMessage(ChatColor.RED + l.getText("unkcmd"));
			cs.sendMessage(ChatColor.RED + l.getText("gethelp", "/" + cmdName + " help"));
			return true;
		}
		
		ACommand annot = cmds.get(args[0].toLowerCase()).getAnnotation(ACommand.class);
		if(!(annot.minArgs() <= args.length - 1 &&
				(annot.maxArgs() >= args.length - 1 || annot.maxArgs() == -1)))
		{
			cs.sendMessage(ChatColor.RED + l.getText("arglen"));
			cs.sendMessage(ChatColor.RED + l.getText("getusage", "/" + cmdName + " help " + args[0].toLowerCase()));
			return true;
		}
		
		if(annot.playerNeed() && !(cs instanceof Player))
		{
			cs.sendMessage(ChatColor.RED + l.getText("beplayer"));
			return true;
		}
		
		// Permissions prüfen
		if(!checkPerm(cs, annot, true))
			return true;
			
		try
		{
			cmds.get(args[0].toLowerCase()).invoke(cmdListener, cs, newargs);
		} catch (InvocationTargetException e)
		{
			cs.sendMessage(ChatColor.RED + l.getText("error"));
			if(e.getCause() != null)
				e.getCause().printStackTrace();
		} catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return true; // Immer true, da es eine automatische Hilfe gibt :)
	}

	@Override
	public List<String> onTabComplete(CommandSender cs,
			org.bukkit.command.Command cmd, String label, String[] args)
	{
		ArrayList<String> ret = new ArrayList<String>();
		if(args.length == 1)
			for(String akt : cmds.keySet())
				if(akt.startsWith(args[0]) && checkPerm(cs, cmds.get(akt).getAnnotation(ACommand.class), false))
					ret.add(akt);
		return ret;
	}
}
