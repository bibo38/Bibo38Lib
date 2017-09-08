package me.bibo38.Bibo38Lib.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.bibo38.Bibo38Lib.Permissions;
import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.config.Language;

public class CommandHandler extends Startfunc implements CommandExecutor, TabCompleter
{
	private static ChatColor col;
	
	protected JavaPlugin plug;
	
	private String cmdName;
	private CommandListener cmdListener;
	private HashMap<String, CommandMethod> cmds = new HashMap<String, CommandMethod>();
	private boolean direct = false;
	private Language lang = null;
	
	private Permissions perm;
	
	/**
	 * Konstruktor um ein neues Kommando hinzuzufügen
	 * 
	 * @param main Das JavaPlugin
	 * @param perms Der Permission-Baum
	 * @param cmdName Der name des Kommandos
	 * @param cmdListener Der Listener für das Kommando
	 */
	public CommandHandler(JavaPlugin main, String perms, String cmdName, CommandListener cmdListener)
	{
		plug = main;
		this.cmdListener = cmdListener;
		this.cmdName = cmdName;
		perm = new Permissions(perms);
		updateColor();
				
		// Den Listener untersuchen
		for(Method m : cmdListener.getClass().getMethods())
		{
			if(m.getAnnotation(Command.class) != null)
			{
				// Funktion auslesen
				String name = m.getName().toLowerCase();
				cmds.put(name, new CommandMethod(m));
			}
		}
		
		PluginCommand cmd = plug.getCommand(cmdName);
		if(cmd == null)
			throw new IllegalArgumentException("Not registered Command " + cmdName);
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
	}
	
	/**
	 * Konstruktor um Direkkommandos hinzuzufügen
	 * 
	 * @param main Das JavaPlugin
	 * @param cmdListener Der Listener für das Kommando
	 * @param perms Der Permission-Baum
	 */
	public CommandHandler(JavaPlugin main, CommandListener cmdListener, String perms)
	{
		plug = main;
		this.cmdListener = cmdListener;
		perm = new Permissions(perms);
		updateColor();
		direct = true;
				
		// Den Listener untersuchen
		for(Method m : cmdListener.getClass().getMethods())
		{
			Command annot = m.getAnnotation(Command.class);
			if(annot != null)
			{
				// Funktion auslesen
				String name = m.getName().toLowerCase();
				
				PluginCommand cmd = plug.getCommand(name);
				if(cmd == null)
					throw new IllegalArgumentException("Not registered Command " + name);
				cmds.put(name, new CommandMethod(m));
				cmd.setExecutor(this);
				cmd.setTabCompleter(this);
			}
		}
	}
	
	/**
	 * Konstruktor um Direkkommandos hinzuzufügen
	 * 
	 * @param main Das JavaPlugin
	 * @param cmdListener Der Listener für das Kommando
	 * @param perms Der Permission-Baum
	 * @param lang Die Sprachdatei für die Hilfeseiten
	 */
	public CommandHandler(JavaPlugin main, CommandListener cmdListener, String perms, Language lang)
	{
		this(main, cmdListener, perms);
		this.lang = lang;
	}
	
	/**
	 * Konstruktor um ein neues Kommando hinzuzufügen
	 * 
	 * @param emain Das JavaPlugin
	 * @param ecmdName Der Name des Kommandos
	 * @param ecmdListener Der Listener für das Kommando
	 */
	public CommandHandler(JavaPlugin main, String cmdName, CommandListener cmdListener)
	{
		this(main, cmdName.toLowerCase(), cmdName, cmdListener);
	}
	
	/**
	 * Konstruktor um ein neues Kommando hinzuzufügen
	 * 
	 * @param main Das JavaPlugin
	 * @param perms Der Permission-Baum
	 * @param cmdName Der name des Kommandos
	 * @param cmdListener Der Listener für das Kommando
	 * @param lang Die Sprachdatei für die Hilfeseiten
	 */
	public CommandHandler(JavaPlugin main, String perms, String cmdName, CommandListener cmdListener, Language lang)
	{
		this(main, perms, cmdName, cmdListener);
		this.lang = lang;
	}
	
	/**
	 * Konstruktor um ein neues Kommando hinzuzufügen
	 * 
	 * @param main Das JavaPlugin
	 * @param cmdName Der name des Kommandos
	 * @param cmdListener Der Listener für das Kommando
	 * @param lang Die Sprachdatei für die Hilfeseiten
	 */
	public CommandHandler(JavaPlugin main, String cmdName, CommandListener cmdListener, Language lang)
	{
		this(main, cmdName.toLowerCase(), cmdName, cmdListener);
		this.lang = lang;
	}
	
	public static void updateColor()
	{
		col = ChatColor.getByChar(main.getConfig().getString("helpcolor"));
	}

	private String translate(String type, String funcName, String defaultVal)
	{
		if(lang == null)
			return defaultVal;
		
		Language akt = lang;
		if(!direct)
			akt = akt.getSection(cmdName);
		akt = akt.getSection(funcName);
		
		if(akt.existKey(type, false))
			return akt.getText(type);
		else
		{
			akt.getCfg().set(type, defaultVal);
			akt.getCfg().save();
			return defaultVal;
		}
	}
	
	private String translate(String type, CommandMethod m, String defaultVal)
	{
		return this.translate(type, m.getName(), defaultVal);
	}
	
	private boolean checkPerm(CommandSender cs, Command annot, boolean show, String methodName)
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
				return true;
			else if(annot.permissions().equals(""))
			{
				if(perm.hasPerm(cs, methodName.toLowerCase(), show))
					return true;
			} else if(perm.hasPerm(cs, annot.permissions(), show))
				return true;
			
			return false;
		} else
			return true;
	}
	
	private void sendHelp(CommandSender cs, int page)
	{
		String useCmdName = direct? "" : (cmdName + " ");
		// Seitenanzahl berechnen
		if(page < 1)
			page = 1;
		
		TreeMap<String, Command> allowedCmds = new TreeMap<String, Command>();
		for(String aktCmd : cmds.keySet().toArray(new String[0]))
		{
			Command annot = cmds.get(aktCmd).getAnnotation();
			if(checkPerm(cs, annot, false, aktCmd))
				allowedCmds.put(aktCmd, annot);
		}
		
		int pages = allowedCmds.keySet().size() / 10 + 1;
		if(page > pages)
			page = pages;
		
		cs.sendMessage(col + main.lang.getText("help", plug.getName() + " " + plug.getDescription().getVersion(), String.valueOf(page), String.valueOf(pages)));
		cs.sendMessage("");
		
		String cmdArray[] = allowedCmds.keySet().toArray(new String[0]);
		for(int i = 10 * (page - 1); i < 10 * page && i < cmdArray.length; i++)
		{
			String name = cmdArray[i];
			Command aktcmd = allowedCmds.get(name);
			
			cs.sendMessage(col + "/" + useCmdName + name + " - " + translate("description", cmds.get(name), aktcmd.description()));
		}
		
		cs.sendMessage("");
		if(page != pages)
			cs.sendMessage(col + main.lang.getText("next", "/" + useCmdName + "help " + (page + 1)));
		
		cs.sendMessage(col + "----------------------------------------");
	}
	
	private void sendHelp(CommandSender cs, String command)
	{
		CommandMethod m = cmds.get(command.toLowerCase());
		if(m == null)
		{
			cs.sendMessage(ChatColor.RED + main.lang.getText("unkcmd"));
			cs.sendMessage("");
			this.sendHelp(cs, 1);
			return;
		}
		
		Command annot = m.getAnnotation();
		cs.sendMessage(col + "------- " + m.getName() + " -------");
		cs.sendMessage("");
	
		String useCmdName = direct? "" : (cmdName + " ");
		Language l = main.lang;
		cs.sendMessage(col + l.getText("usage") + "/" + useCmdName + m.getName() + " " + translate("usage", m, annot.usage()));
		cs.sendMessage(col + l.getText("desc") + translate("description", m, annot.description()));
		cs.sendMessage(col + l.getText("console") + (m.isPlayerNeeded()? l.getText("noLang") : l.getText("yesLang")));
		
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
	
	private boolean tryCustomHelp(CommandSender cs, String args[])
	{
		if(cmds.containsKey("help"))
		{
			try
			{
				cmds.get("help").invoke(cmdListener, cs, args);
			} catch(Exception e)
			{
				cs.sendMessage(ChatColor.RED + main.lang.getText("error"));
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Die onCommand Funktion für ankommende
	 * Befehle
	 */
	@Override
	public boolean onCommand(CommandSender cs,
			org.bukkit.command.Command cmd, String label, String args[])
	{
		Language l = main.lang;
		String useCmd = direct? cmd.getName().toLowerCase() : cmdName;

		if(!direct && (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")))) // Hilfe anzeigen
		{
			if(!tryCustomHelp(cs, new String[0]))
				this.sendHelp(cs, 1);
			return true;
		}
		
		// Neue Args erstellen
		String newargs[] = direct? args : Arrays.copyOfRange(args, 1, args.length);
		String aktCmdName = direct? useCmd : args[0].toLowerCase();
		
		if(args.length > 0 && args[0].equalsIgnoreCase("help"))
		{
			if(!tryCustomHelp(cs, direct? Arrays.copyOfRange(args,  1,  args.length) : newargs))
			{
				if(direct)
					this.sendHelp(cs, aktCmdName);
				else
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
					
					if(command.isEmpty())
					{
						this.sendHelp(cs, seite);
					} else
					{
						this.sendHelp(cs, command);
					}
				}
			}
			
			return true;
		}
				
		if(!cmds.containsKey(aktCmdName))
		{
			cs.sendMessage(ChatColor.RED + l.getText("unkcmd"));
			cs.sendMessage(ChatColor.RED + l.getText("gethelp", "/" + useCmd + " help"));
			return true;
		}
		
		CommandMethod m = cmds.get(aktCmdName);
		
		if(!(m.getMinArgs() <= newargs.length && (m.getMaxArgs() == -1 || m.getMaxArgs() >= newargs.length)))
		{
			cs.sendMessage(ChatColor.RED + l.getText("arglen"));
			cs.sendMessage(ChatColor.RED + l.getText("getusage", "/" + useCmd + " help" + (direct? "" : " " + args[0].toLowerCase())));
			return true;
		}
		
		if(m.isPlayerNeeded() && !(cs instanceof Player))
		{
			cs.sendMessage(ChatColor.RED + l.getText("beplayer"));
			return true;
		}
		
		// Permissions prüfen
		if(!checkPerm(cs, m.getAnnotation(), true, aktCmdName))
			return true;
			
		m.invoke(cmdListener, cs, newargs);
		return true; // Immer true, da es eine automatische Hilfe gibt :)
	}

	@Override
	public List<String> onTabComplete(CommandSender cs,
			org.bukkit.command.Command cmd, String label, String args[])
	{
		ArrayList<String> ret = new ArrayList<String>();
		if(!direct && args.length == 1)
		{
			if("help".regionMatches(true, 0, args[0], 0, args[0].length()))
				ret.add("help");
			for(String akt : cmds.keySet())
				if(akt.regionMatches(true, 0, args[0], 0, args[0].length()) && checkPerm(cs, cmds.get(akt).getAnnotation(), false, akt))
					ret.add(akt);
			return ret;
		}
		
		CommandMethod cmdMeth = cmds.get(direct? cmd.getName().toLowerCase() : args[0]);
		if(cmdMeth != null)
			cmdMeth.onTabComplete(ret, direct? args : Arrays.copyOfRange(args, 1, args.length));
		else if(!direct && args[0].equalsIgnoreCase("help"))
			for(String akt : cmds.keySet())
				if(akt.regionMatches(true, 0, args[1], 0, args[1].length()) && checkPerm(cs, cmds.get(akt).getAnnotation(), false, akt))
					ret.add(akt);
		return ret;
	}
}