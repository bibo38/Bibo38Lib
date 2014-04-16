package me.bibo38.Bibo38Lib;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Utils
{
	// Reflection Utils
	public static void setVal(Field f, Object o, Object val) throws IllegalArgumentException, IllegalAccessException
	{
		boolean access = f.isAccessible();
		f.setAccessible(true);
		if(val.getClass() != f.getType())
			val = convert(val, f.getType());
		f.set(o, val);
		f.setAccessible(access);
	}
	
	public static Class<?> getWrapper(Class<?> c)
	{
		if(!c.isPrimitive())
			return c;
		else if(c == boolean.class)
			return Boolean.class;
		else if(c == char.class)
			return Character.class;
		else if(c == byte.class)
			return Byte.class;
		else if(c == short.class)
			return Short.class;
		else if(c == int.class)
			return Integer.class;
		else if(c == long.class)
			return Long.class;
		else if(c == float.class)
			return Float.class;
		else if(c == double.class)
			return Double.class;
		else if(c == void.class)
			return Void.class;
		else
			return c;
	}
	
	public static Object convert(Object o, Class<?> c)
	{
		Object ret = o;
		c = getWrapper(c);
		try
		{
			String s = o.toString();
			ret = c.getMethod("valueOf", String.class).invoke(null, s);
		} catch(Exception e)
		{
			try
			{
				ret = c.cast(o);
			} catch(Exception e1)
			{
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public static Object getVal(Field f, Object o) throws IllegalArgumentException, IllegalAccessException
	{
		Object ret;
		boolean access = f.isAccessible();
		f.setAccessible(true);
		ret = f.get(o);
		f.setAccessible(access);
		return ret;
	}
	
	public static void saveLocation(ConfigurationSection cfs, Location loc)
	{
		if(loc == null)
			return;
		cfs.set("x", loc.getX());
		cfs.set("y", loc.getY());
		cfs.set("z", loc.getZ());
		cfs.set("world", loc.getWorld().getName());
	}
	
	public static Location getLocation(ConfigurationSection cfg, String name)
	{
		cfg = cfg.getConfigurationSection(name);
		if(cfg == null)
			return null;
		return new Location(Bukkit.getWorld(cfg.getString("world")),
							 cfg.getDouble("x"),
							 cfg.getDouble("y"),
							 cfg.getDouble("z"));
	}
	
	public static void clearInventory(Player p)
	{
		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
	}
	
	public static OfflinePlayer getSkullName(Location l)
	{
		if(l == null || l.getBlock().getType() != Material.SKULL)
			return null;
		Skull s = (Skull) l.getBlock().getState();
		return s.getPlayer();
	}
	
	public static void setSkullName(Location l, Player p)
	{
		if(l == null || l.getBlock().getType() != Material.SKULL)
			return;
		Skull s = (Skull) l.getBlock().getState();
		s.setSkullType(SkullType.PLAYER);
		s.setPlayer(p);
	}
	
	public static String getSkullName(ItemStack i)
	{
		if(i == null || i.getType() != Material.SKULL_ITEM)
			return null;
		SkullMeta s = (SkullMeta) i.getItemMeta();
		return s.getOwner();
	}
	
	public static void setSkullName(ItemStack i, String name)
	{
		if(i == null || i.getType() != Material.SKULL_ITEM)
			return;
		i.setDurability((short) SkullType.PLAYER.ordinal());
		SkullMeta s = (SkullMeta) i.getItemMeta();
		s.setOwner(name);
		i.setItemMeta(s);
	}
	
	public static void setItemName(ItemStack i, String name)
	{
		if(i == null)
			return;
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
		i.setItemMeta(im);
	}
	
	public static String getItemName(ItemStack i)
	{
		if(i == null)
			return null;
		return i.getItemMeta().getDisplayName();
	}
}
