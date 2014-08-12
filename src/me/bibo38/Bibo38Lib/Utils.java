package me.bibo38.Bibo38Lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.util.com.mojang.authlib.Agent;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.GameProfileRepository;
import net.minecraft.util.com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.util.com.mojang.authlib.minecraft.MinecraftSessionService;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
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
	
	public static boolean isPrimitiveType(Class<?> c)
	{
		return c.isPrimitive() || c == Boolean.class || c == Character.class || c == Byte.class || c == Short.class || c == Integer.class || c == Long.class || c == Float.class || c == Double.class || c == Void.class;
	}
	
	public static Object convert(Object o, Class<?> c)
	{
		Object ret = o;
		c = getWrapper(c);
		
		if(isPrimitiveType(c))
		{
			try
			{
				String s = o.toString();
				ret = c.getMethod("valueOf", String.class).invoke(null, s);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
		} else if(isPrimitiveType(o.getClass()) && c == String.class)
			return o.toString();
		
		try
		{
			ret = c.cast(o); // try casting
		} catch(Exception e1)
		{
			// Try serialisation
			if(o instanceof String && Serializable.class.isAssignableFrom(c))
			{
				try
				{
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode((String) o)));
					ret = ois.readObject();
				} catch(Exception e2)
				{
					e2.printStackTrace();
				}
			} else if(c == String.class && o instanceof Serializable)
			{
				try
				{
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject(o);
					oos.flush();
					return Base64.encode(bos.toByteArray());
				} catch(Exception e2)
				{
					e2.printStackTrace();
				}
			} else
				e1.printStackTrace();
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
	
	public static void saveLocation(ConfigurationSection cfs, String name, Location loc)
	{
		if(cfs != null)
			saveLocation(cfs.createSection(name), loc);
	}
	
	public static Location getLocation(ConfigurationSection cfg, String name)
	{
		cfg = cfg.getConfigurationSection(name);
		if(cfg == null || !cfg.contains("world") || !cfg.contains("x") || !cfg.contains("y") || !cfg.contains("z"))
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
	
	public static String getSkullName(Location l)
	{
		if(l == null || l.getBlock().getType() != Material.SKULL)
			return null;
		Skull s = (Skull) l.getBlock().getState();
		return s.getOwner();
	}
	
	public static void setSkullName(Location l, String name)
	{
		if(l == null || l.getBlock().getType() != Material.SKULL)
			return;
		Skull s = (Skull) l.getBlock().getState();
		s.setSkullType(SkullType.PLAYER);
		s.setOwner(name);
		s.update(false, false);
	}
	
	public static String getSkullName(ItemStack i)
	{
		if(i == null || i.getType() != Material.SKULL_ITEM)
			return null;
		SkullMeta s = (SkullMeta) i.getItemMeta();
		return s.getOwner();
	}
	
	public static ItemStack getSkullWithName(String name)
	{
		ItemStack i = null;
		// Bukkit Stuff, not working properly for the UUID system
		/* SkullMeta s = (SkullMeta) i.getItemMeta();
		s.setOwner(name);
		i.setItemMeta(s); */
		
		// A little Hack
		try
		{
			Class<?> cbitemstack = getCBClass("inventory.CraftItemStack");
			i = (ItemStack) cbitemstack.getMethod("asCraftCopy", ItemStack.class).invoke(null, new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal()));
			
			Class<?> mcServerClass = getMCClass("MinecraftServer");
			Object mcServer = mcServerClass.getMethod("getServer").invoke(null);
			GameProfileRepository repo = (GameProfileRepository) mcServerClass.getMethod("getGameProfileRepository").invoke(mcServer);
			final MinecraftSessionService session = (MinecraftSessionService) mcServerClass.getMethod("av").invoke(mcServer);
			
			Field handle = cbitemstack.getDeclaredField("handle");
			handle.setAccessible(true);
			final Object itemstack = handle.get(i);
			handle.setAccessible(false);
			
			repo.findProfilesByNames(new String[] { name }, Agent.MINECRAFT, new ProfileLookupCallback() {
				@Override
				public void onProfileLookupSucceeded(GameProfile p)
				{
					try
					{
						session.fillProfileProperties(p, true);
						Class<?> compound = getMCClass("NBTTagCompound");
						Object outerCompound = compound.getConstructor().newInstance();
						Object innerCompound = compound.getConstructor().newInstance();
						
						getMCClass("GameProfileSerializer").getMethod("a", compound, GameProfile.class).invoke(null, innerCompound, p);
						compound.getMethod("set", String.class, getMCClass("NBTBase")).invoke(outerCompound, "SkullOwner", innerCompound);
						getMCClass("ItemStack").getMethod("setTag", compound).invoke(itemstack, outerCompound);
						System.out.println(outerCompound);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
				@Override
				public void onProfileLookupFailed(GameProfile arg0, Exception arg1) {}
			});
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return i;
	}
	
	public static void heal(Player p)
	{
		p.setHealthScale(1D);
		p.setFoodLevel(20);
		p.setSaturation(4.0F);
		p.setExhaustion(0);
	}
	
	public static void setItemName(ItemStack i, String name)
	{
		if(i == null)
			return;
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
		i.setItemMeta(im);
	}
	
	public static UUID getUUID(String s)
	{
		try
		{
			return UUID.fromString(s);
		} catch(Exception e) {}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Player getPlayer(String name)
	{
		for(Player akt : Bukkit.getOnlinePlayers())
			if(akt.getName().equals(name))
				return akt;
		return Bukkit.getPlayer(getUUID(name));
	}
	
	public static OfflinePlayer[] getOfflinePlayers(String name)
	{
		HashSet<OfflinePlayer> ret = new HashSet<OfflinePlayer>();
		for(OfflinePlayer akt : Bukkit.getOfflinePlayers())
			if(akt.getName().equals(name))
				ret.add(akt);
		if(Bukkit.getOfflinePlayer(UUID.fromString(name)) != null)
			ret.add(Bukkit.getOfflinePlayer(UUID.fromString(name)));
		return ret.toArray(new OfflinePlayer[0]);
	}
	
	public static String getItemName(ItemStack i)
	{
		if(i == null)
			return null;
		return i.getItemMeta().getDisplayName();
	}
	
	public static String getPackageVersion()
	{
		// getName() -> org.bukkit.craftbukkit.v1_7_R3.CraftServer
		return Bukkit.getServer().getClass().getName().split(Pattern.quote("."))[3];
	}
	
	public static Class<?> getCBClass(String name)
	{
		try
		{
			return Class.forName("org.bukkit.craftbukkit." + getPackageVersion() + "." + name);
		} catch (ClassNotFoundException e)
		{
			return null;
		}
	}
	
	public static Class<?> getMCClass(String name)
	{
		try
		{
			return Class.forName("net.minecraft.server." + getPackageVersion() + "." + name);
		} catch (ClassNotFoundException e)
		{
			return null;
		}
	}
	
	public static Object getEntityHandle(Entity ent)
	{
		try
		{
			return ent.getClass().getMethod("getHandle").invoke(ent);
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object createPacket(String name, Object... args)
	{
		Class<?> packClass = getMCClass("Packet" + name);
		Constructor<?> constr[] = packClass.getConstructors();
		
		try
		{
			for(Constructor<?> akt : constr)
				if(akt.getParameterTypes().length == args.length)
					return akt.newInstance(args);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
			
		return null;
	}
	
	public static void sendPacket(Object player, Object pack)
	{
		try
		{
			Object playerConnection = player.getClass().getField("playerConnection").get(player);
			playerConnection.getClass().getMethod("sendPacket", getMCClass("Packet")).invoke(playerConnection, pack);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendPacket(Object pack, Player... players)
	{
		for(Player p : players)
			sendPacket(getEntityHandle(p), pack);
	}
}
