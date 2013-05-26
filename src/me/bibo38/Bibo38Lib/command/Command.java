package me.bibo38.Bibo38Lib.command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import me.bibo38.Bibo38Lib.Permissions;
import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.config.Language;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Command extends Startfunc implements CommandExecutor
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
				if(m.getParameterTypes().length == 2 &&
						m.getParameterTypes()[0].equals(CommandSender.class) &&
						m.getParameterTypes()[1].equals(String[].class))
				{
					cmds.put(name, m);
				}
			}
		}
		
		plug.getCommand(cmdName).setExecutor(this);
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
				else
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
		{
			page = 1;
		}
		
		HashMap<String, ACommand> allowedCmds = new HashMap<String, ACommand>();
		for(String aktCmd : cmds.keySet().toArray(new String[0]))
		{
			ACommand annot = cmds.get(aktCmd).getAnnotation(ACommand.class);
			if(checkPerm(cs, annot, false))
				allowedCmds.put(aktCmd, annot);
		}
		
		int pages = allowedCmds.keySet().size() / 10 + 1;
		if(page > pages)
		{
			page = pages;
		}
		
		cs.sendMessage(col + main.lang.getText("help", plug.getName()+" "+plug.getDescription().getVersion(), String.valueOf(page), String.valueOf(pages)));
		cs.sendMessage("");
		
		Iterator<String> it = allowedCmds.keySet().iterator();
		while(it.hasNext())
		{
			String name = it.next();
			ACommand aktcmd = allowedCmds.get(name);
			
			cs.sendMessage(col + "/" + cmdName + " " + name + " - " + aktcmd.description());
		}
		
		cs.sendMessage("");
		if(page != pages)
		{
			cs.sendMessage(col + main.lang.getText("next", "/" + cmdName + " help " + (page + 1)));
		}
		
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
		if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) // Hilfe anzeigen
		{
			this.sendHelp(cs, 1);
			return true;
		}
		
		if(args[0].equalsIgnoreCase("help"))
		{
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
		
		Language l = main.lang;
		
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
		
		// Neue Args erstellen
		String[] newargs = new String[args.length - 1];
		for(int i = 1; i < args.length; i++)
		{
			newargs[i - 1] = args[i];
		}
		
		try
		{
			cmds.get(args[0].toLowerCase()).invoke(cmdListener, cs, newargs);
		} catch (Exception e)
		{
			cs.sendMessage(ChatColor.RED + l.getText("error"));
			e.printStackTrace();
		}
		
		return true; // Immer true, da es eine automatische Hilfe gibt :)
	}
}
