package me.bibo38.Bibo38Lib.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.block.Block;

import me.bibo38.Bibo38Lib.database.annotations.Id;
import me.bibo38.Bibo38Lib.database.annotations.Table;

@Table(name = "BlockMeta")
public class BlockMetaEntry
{
	public UUID world;
	public int blockX, blockY, blockZ;
	public HashMap<String, HashSet<Serializable>> meta = new HashMap<String, HashSet<Serializable>>();
	
	@Id
	private int id;
	
	public BlockMetaEntry() {}
	
	public BlockMetaEntry(Block b)
	{
		world = b.getWorld().getUID();
		blockX = b.getX();
		blockY = b.getY();
		blockZ = b.getZ();
	}
}
