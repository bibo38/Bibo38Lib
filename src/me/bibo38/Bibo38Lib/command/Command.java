package me.bibo38.Bibo38Lib.command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import me.bibo38.Bibo38Lib.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Command implements CommandExecutor
{
	protected JavaPlugin main;
	
	private String cmdName;
	private CommandListener cmdListener;
	private HashMap<String, Method> cmds;
	
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
		main = emain;
		cmdListener = ecmdListener;
		cmdName = ecmdName;
		perm = new Permissions(perms);
		
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
		
		main.getCommand(cmdName).setExecutor(this);
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

	private void sendHelp(CommandSender cs, int page)
	{
		// Seitenanzahl berechnen
		if(page < 1)
		{
			page = 1;
		}
		
		int pages = cmds.size() / 10 + 1;
		if(page > pages)
		{
			page = pages;
		}
		
		cs.sendMessage(ChatColor.YELLOW + "------- Help for " + main.getName() + " " + main.getDescription().getVersion() +
				" Page " + page + "/" + pages + " -------");
		cs.sendMessage("");
		
		Iterator<String> it = cmds.keySet().iterator();
		while(it.hasNext())
		{
			String name = it.next();
			ACommand aktcmd = cmds.get(name).getAnnotation(ACommand.class);
			
			cs.sendMessage(ChatColor.YELLOW + "/" + cmdName + " " + name + " - " + aktcmd.description());
		}
		
		cs.sendMessage("");
		if(page != pages)
		{
			cs.sendMessage(ChatColor.YELLOW + "Type /" + cmdName + " help " + (page + 1) + " to view the next page!");
		}
		
		cs.sendMessage(ChatColor.YELLOW + "----------------------------------------");
	}
	
	private void sendHelp(CommandSender cs, String command)
	{
		Method m = cmds.get(command.toLowerCase());
		if(m == null)
		{
			cs.sendMessage(ChatColor.YELLOW + "Command not found!");
			cs.sendMessage("");
			this.sendHelp(cs, 1);
			return;
		}
		
		ACommand annot = m.getAnnotation(ACommand.class);
		cs.sendMessage(ChatColor.YELLOW + "------- " + command.toLowerCase() + " -------");
		cs.sendMessage("");
		
		cs.sendMessage(ChatColor.YELLOW + "Usage: /" + cmdName + " " + command.toLowerCase() + " " + annot.usage());
		cs.sendMessage(ChatColor.YELLOW + "Description: " + annot.description());
		cs.sendMessage(ChatColor.YELLOW + "Console Allowed: " + (annot.playerNeed() ? "No" : "Yes"));
		
		if(annot.permissions().equals("op") || annot.permissions().equals("none"))
		{
			cs.sendMessage(ChatColor.YELLOW + "Permissions: " + annot.permissions());
		} else
		{
			cs.sendMessage(ChatColor.YELLOW + "Permissions: " + perm.getFather() + "." + annot.permissions());
		}
		
		cs.sendMessage("");
		cs.sendMessage(ChatColor.YELLOW + "-------------------");
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
		
		if(!cmds.containsKey(args[0].toLowerCase()))
		{
			cs.sendMessage(ChatColor.RED + "Unknown Command!");
			cs.sendMessage(ChatColor.RED + "Type /" + cmdName + " help to get all avaible Commands!");
			return true;
		}
		
		ACommand annot = cmds.get(args[0].toLowerCase()).getAnnotation(ACommand.class);
		if(!(annot.minArgs() <= args.length - 1 &&
				(annot.maxArgs() >= args.length - 1 || annot.maxArgs() == -1)))
		{
			cs.sendMessage(ChatColor.RED + "Invalid argument length!");
			cs.sendMessage(ChatColor.RED + "Type /" + cmdName + " help to get the Command usage!");
			return true;
		}
		
		if(annot.playerNeed() && !(cs instanceof Player))
		{
			cs.sendMessage(ChatColor.RED + "You must be a Player to execute the Command!");
			return true;
		}
		
		// Permissions prüfen
		if(cs instanceof Player)
		{
			boolean access = false;
			if(annot.permissions().equals("op"))
			{
				if(((Player) cs).isOp())
				{
					access = true;
				} else
				{
					cs.sendMessage(ChatColor.RED + "You don't have permissions to perform this operation!");
				}
			} else if(annot.permissions().equals("none"))
			{
				access = true;
			} else if(perm.hasPerm(cs, annot.permissions()))
			{
				access = true;
			}
			
			if(!access) // Pech gehabt
			{
				return true;
			}
		}
		
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
			cs.sendMessage(ChatColor.RED + "Error executing Command! See the Server Log!");
			e.printStackTrace();
		}
		
		return true; // Immer true, da es eine automatische Hilfe gibt :)
	}
}
