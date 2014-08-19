package me.bibo38.Bibo38Lib.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.block.Block;

@Table(name = "BlockMeta")
public class BlockMetaEntry
{
	@Id
	private int id;
	
	public UUID world;
	public int blockX, blockY, blockZ;
	public HashMap<String, HashSet<Serializable>> meta = new HashMap<String, HashSet<Serializable>>();
	
	public BlockMetaEntry() {}
	
	public BlockMetaEntry(Block b)
	{
		world = b.getWorld().getUID();
		blockX = b.getX();
		blockY = b.getY();
		blockZ = b.getZ();
	}
}
