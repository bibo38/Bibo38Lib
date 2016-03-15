package me.bibo38.Bibo38Lib.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.Utils;

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
				for(Annotation curAnnot : m.getParameterAnnotations()[i])
					if(curAnnot.annotationType() == Optional.class)
						minArgs--;
				minArgs++;
			}
			maxArgs++;
		}
		if(params.length > 0 && params[params.length - 1].isArray())
			maxArgs = -1; // Infinite argument size
	}

	@SuppressWarnings("unchecked")
	public boolean invoke(CommandListener cmdListener, CommandSender cs, String givenArgs[])
	{
		Object args[] = new Object[m.getParameterTypes().length];
		
		int i = 0;
		if(commandSenderNeeded)
			args[i++] = cs;
		for(int j = 0; j < givenArgs.length; j++, i++)
		{
			if(i >= args.length)
				break;
			
			if(i == (args.length - 1) && maxArgs == -1)
			{
				// Infinite  Arguments, copy the rest
				Object arr[] = new Object[givenArgs.length - j];
				for(int k = 0; k < arr.length; k++)
				{
					arr[k] = convertTo(givenArgs[j + k], params[i].getComponentType());
					if(arr[k] == null)
					{
						main.lang.sendText(cs, "unknown", true, params[i].getName() + " " + givenArgs[j + k]);
						return false;
					}
				}
				
				args[i] = Arrays.copyOf(arr, arr.length, (Class<? extends Object[]>) params[i]);
				break;
			}
			
			args[i] = convertTo(givenArgs[j], params[i]);
			if(args[i] == null && (commandSenderNeeded? i <= minArgs : i < minArgs))
			{
				main.lang.sendText(cs, "unknown", true, params[i].getName() + " " + givenArgs[j]);
				return false;
			}
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
		int count = commandSenderNeeded? args.length : args.length - 1;
		
		if(maxArgs == -1 && count >= params.length)
			count = params.length - 1;
		if(count >= params.length || count < 0)
			return; // Zu viele/wenige Argumente
		
		Class<?> c = params[count];
		if(c.isArray())
			c = c.getComponentType();
		String last = args[args.length - 1];
		
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
	
	@SuppressWarnings("deprecation")
	private static Object convertTo(String s, Class<?> c)
	{
		if(c == String.class)
			return s;
		UUID id = Utils.getID(s);
		
		if(c == UUID.class)
			return id;
		if(c == Player.class)
			return Utils.getPlayer(s);
		if(c == OfflinePlayer.class)
		{
			if(id != null)
				return Bukkit.getOfflinePlayer(id);
			return Bukkit.getOfflinePlayer(s);
		}
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
			} catch(Exception e)
			{
			}
			return d;
		}
		if(c == float.class || c == Float.class)
		{
			Float f = null;
			try
			{
				f = Float.valueOf(s);
			} catch(Exception e)
			{
			}
			return f;
		}
		if(c == boolean.class || c == Boolean.class)
			return !("false".equalsIgnoreCase(s) || "0".equals(s) || s.isEmpty());
		if(c == byte.class || c == Byte.class)
		{
			Byte b = null;
			try
			{
				b = Byte.valueOf(s);
			} catch(Exception e)
			{
			}
			return b;
		}
		if(c == short.class || c == Short.class)
		{
			Short val = null;
			try
			{
				val = Short.valueOf(s);
			} catch(Exception e)
			{
			}
			return val;
		}
		if(c == int.class || c == Integer.class)
		{
			Integer i = null;
			try
			{
				i = Integer.valueOf(s);
			} catch(Exception e)
			{
			}
			return i;
		}
		if(c == long.class || c == Long.class)
		{
			Long l = null;
			try
			{
				l = Long.valueOf(s);
			} catch(Exception e)
			{
			}
			return l;
		}
		
		return null;
	}
}

