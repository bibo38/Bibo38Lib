package me.bibo38.Bibo38Lib.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import me.bibo38.Bibo38Lib.Permissions;
import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.Utils;
import me.bibo38.Bibo38Lib.config.Language;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandHandler extends Startfunc implements CommandExecutor, TabCompleter
{
	protected JavaPlugin plug;
	
	private String cmdName;
	private CommandListener cmdListener;
	private HashMap<String, CommandMethod> cmds = new HashMap<String, CommandMethod>();
	private static ChatColor col;
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
			throw new IllegalArgumentException("Not registered Command "+cmdName);
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
					throw new IllegalArgumentException("Not registered Command "+name);
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
	
	private boolean checkPerm(CommandSender cs, Command annot, boolean show)
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
		String useCmdName = direct? "" : (cmdName + " ");
		// Seitenanzahl berechnen
		if(page < 1)
			page = 1;
		
		TreeMap<String, Command> allowedCmds = new TreeMap<String, Command>();
		for(String aktCmd : cmds.keySet().toArray(new String[0]))
		{
			Command annot = cmds.get(aktCmd).getAnnotation();
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
		String useCmdName = direct? "" : (cmdName + " ");
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
			org.bukkit.command.Command cmd, String label, String[] args)
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
		String[] newargs = direct? args : Arrays.copyOfRange(args, 1, args.length);
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
					
					if(command.equals(""))
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
		
		if(!(m.getMinArgs() <= newargs.length && m.getMaxArgs() >= newargs.length))
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
		if(!checkPerm(cs, m.getAnnotation(), true))
			return true;
			
		m.invoke(cmdListener, cs, newargs);
		return true; // Immer true, da es eine automatische Hilfe gibt :)
	}

	@Override
	public List<String> onTabComplete(CommandSender cs,
			org.bukkit.command.Command cmd, String label, String[] args)
	{
		ArrayList<String> ret = new ArrayList<String>();
		if(!direct && args.length == 1)
		{
			if("help".regionMatches(true, 0, args[0], 0, args[0].length()))
				ret.add("help");
			for(String akt : cmds.keySet())
				if(akt.regionMatches(true, 0, args[0], 0, args[0].length()) && checkPerm(cs, cmds.get(akt).getAnnotation(), false))
					ret.add(akt);
			return ret;
		}
		
		CommandMethod cmdMeth = cmds.get(direct? cmd.getName().toLowerCase() : args[0]);
		if(cmdMeth != null)
			cmdMeth.onTabComplete(ret, direct? args : Arrays.copyOfRange(args, 1, args.length));
		else if(!direct && args[0].equalsIgnoreCase("help"))
			for(String akt : cmds.keySet())
				if(akt.regionMatches(true, 0, args[1], 0, args[1].length()) && checkPerm(cs, cmds.get(akt).getAnnotation(), false))
					ret.add(akt);
		return ret;
	}
}

class CommandMethod extends Startfunc
{
	private Method m;
	private String name;
	private int minArgs = 0, maxArgs = 0;
	private boolean playerNeeded, commandSenderNeeded;
	private Command annot;
	private Class<?> params[];
	
	protected CommandMethod(Method m)
	{
		this.m = m;
		annot = m.getAnnotation(Command.class);
		name = m.getName().toLowerCase();
		if(annot != null && annot.umlaut())
			name = name.replace("ae", "ä").replace("oe", "ö").replace("ue", "ü");
		
		params = m.getParameterTypes();
		playerNeeded = params.length > 0 && params[0].equals(Player.class);
		commandSenderNeeded = params.length > 0 && CommandSender.class.isAssignableFrom(params[0]);
		
		for(int i = commandSenderNeeded? 1 : 0; i < params.length; i++)
		{
			if(maxArgs == minArgs)
			{
				for(Annotation annot : m.getParameterAnnotations()[i])
					if(annot.annotationType() == Optional.class)
						minArgs--;
				minArgs++;
			}
			maxArgs++;
		}
	}

	public boolean invoke(CommandListener cmdListener, CommandSender cs, String givenArgs[])
	{
		Object args[] = new Object[m.getParameterTypes().length];
		
		int i = 0;
		if(commandSenderNeeded)
			args[i++] = cs;
		for(String akt : givenArgs)
		{
			if(i >= args.length)
				break;
			args[i] = convertTo(akt, params[i]);
			if(args[i] == null && (commandSenderNeeded? i <= minArgs : i < minArgs))
			{
				main.lang.sendText(cs, "unknown", true, params[i].getName() + " " + akt);
				return false;
			}
			i++;
		}
		
		try
		{
			m.invoke(cmdListener, args);
		} catch (InvocationTargetException e)
		{
			main.lang.sendText(cs, "error", true);
			if(e.getCause() != null)
				e.getCause().printStackTrace();
		} catch(IllegalAccessException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Method getMethod()
	{
		return m;
	}

	public String getName()
	{
		return name;
	}

	public int getMinArgs()
	{
		return minArgs;
	}

	public int getMaxArgs()
	{
		return maxArgs;
	}

	public boolean isPlayerNeeded()
	{
		return playerNeeded;
	}

	public boolean isCommandSenderNeeded()
	{
		return commandSenderNeeded;
	}

	public Command getAnnotation()
	{
		return annot;
	}
	
	private void matchPartly(List<String> l, String a, String b) // B Teil von A?
	{
		if(a.regionMatches(true, 0, b, 0, b.length()))
			l.add(a);
	}
	
	public void onTabComplete(List<String> set, String args[])
	{
		int count = commandSenderNeeded? args.length : args.length-1;
		if(count >= params.length || count < 0)
			return; // Zu viele/wenige Argumente
		
		Class<?> c = params[count];
		String last = args[args.length-1];
		
		if(c == Player.class)
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				matchPartly(set, p.getName(), last);
				matchPartly(set, p.getUniqueId().toString(), last);
			}
		} else if(c == OfflinePlayer.class)
		{
			for(OfflinePlayer p : Bukkit.getOfflinePlayers())
			{
				String uuid = p.getUniqueId().toString();
				if(p.getName().equals(last))
					set.add(uuid);
				matchPartly(set, uuid, last);
			}
		} else if(c == World.class)
		{
			for(World w : Bukkit.getWorlds())
			{
				matchPartly(set, w.getName(), last);
				matchPartly(set, w.getUID().toString(), last);
			}
		}
	}
	
	private static Object convertTo(String s, Class<?> c)
	{
		if(c == String.class)
			return s;
		UUID id = Utils.getUUID(s);
		
		if(c == Player.class)
			return Utils.getPlayer(s);
		if(c == OfflinePlayer.class)
			return Bukkit.getOfflinePlayer(id);
		if(c == World.class)
		{
			World w = Bukkit.getWorld(s);
			if(w == null)
				w = Bukkit.getWorld(id);
			return w;
		}
		if(c == double.class || c == Double.class)
		{
			Double d = null;
			try
			{
				d = Double.valueOf(s);
			} catch(Exception e) {}
			return d;
		}
		if(c == float.class || c == Float.class)
		{
			Float f = null;
			try
			{
				f = Float.valueOf(s);
			} catch(Exception e) {}
			return f;
		}
		if(c == boolean.class || c == Boolean.class)
			return !(s.equalsIgnoreCase("false") || s.equals("0") || s.isEmpty());
		if(c == byte.class || c == Byte.class)
		{
			Byte b = null;
			try
			{
				b = Byte.valueOf(s);
			} catch(Exception e) {}
			return b;
		}
		if(c == short.class || c == Short.class)
		{
			Short val = null;
			try
			{
				val = Short.valueOf(s);
			} catch(Exception e) {}
			return val;
		}
		if(c == int.class || c == Integer.class)
		{
			Integer i = null;
			try
			{
				i = Integer.valueOf(s);
			} catch(Exception e) {}
			return i;
		}
		if(c == long.class || c == Long.class)
		{
			Long l = null;
			try
			{
				l = Long.valueOf(s);
			} catch(Exception e) {}
			return l;
		}
		
		return null;
	}
}
