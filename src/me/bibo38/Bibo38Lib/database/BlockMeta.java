package me.bibo38.Bibo38Lib.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class BlockMeta
{
	private static Database db = null;
	
	private HashMap<Location, BlockMetaEntry> entries = new HashMap<Location, BlockMetaEntry>();
	private Plugin pl;
	
	public BlockMeta(Plugin pl) throws Exception
	{
		if(db == null)
			db = new Database(BlockMetaEntry.class);
		
		this.pl = pl;
		
		reload();
	}
	
	public void reload()
	{
		entries.clear();
		Object[] data = db.find(BlockMetaEntry.class).find();
		for(Object o : data)
		{
			BlockMetaEntry akt = (BlockMetaEntry) o;
			entries.put(new Location(Bukkit.getWorld(akt.world), akt.blockX, akt.blockY, akt.blockZ), akt);
		}
	}
	
	public void add(Block b, Serializable val)
	{
		BlockMetaEntry ent = entries.get(b.getLocation());
		if(ent == null)
			ent = new BlockMetaEntry(b);
		
		HashSet<Serializable> data = ent.meta.get(pl.getName());
		if(data == null)
			data = new HashSet<Serializable>();
		data.add(val);
		ent.meta.put(pl.getName(), data);
		db.save(ent);
	}
	
	public void set(Block b, HashSet<Serializable> data)
	{
		BlockMetaEntry ent = entries.get(b.getLocation());
		if(ent == null)
			ent = new BlockMetaEntry(b);
		
		ent.meta.put(pl.getName(), data);
		db.save(ent);
	}
	
	public void set(Block b, Serializable s)
	{
		BlockMetaEntry ent = entries.get(b.getLocation());
		if(ent == null)
			ent = new BlockMetaEntry(b);
		
		HashSet<Serializable> data = ent.meta.get(pl.getName());
		if(data == null)
			data = new HashSet<Serializable>();
		data.clear();
		data.add(s);
		
		ent.meta.put(pl.getName(), data);
		db.save(ent);
	}
	
	public Serializable getEntry(Block b)
	{
		BlockMetaEntry ent = entries.get(b.getLocation());
		if(ent == null)
			return null;
		
		HashSet<Serializable> data = ent.meta.get(pl.getName());
		if(data == null || data.isEmpty())
			return null;
		else
			return data.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	public HashSet<Serializable> getEntries(Block b)
	{
		BlockMetaEntry ent = entries.get(b.getLocation());
		if(ent == null)
			return new HashSet<Serializable>();
		
		HashSet<Serializable> data = ent.meta.get(pl.getName());
		if(data == null)
			return new HashSet<Serializable>();
		else
			return (HashSet<Serializable>) data.clone();
	}
}
