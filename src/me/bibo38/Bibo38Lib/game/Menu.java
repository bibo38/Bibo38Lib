package me.bibo38.Bibo38Lib.game;

import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;

public class Menu extends Startfunc implements Listener
{
	private static int idCnt = 0;
	private int id;
	private Inventory inv;
	private HashSet<HumanEntity> canClose = new HashSet<HumanEntity>();
	private MenuListener l;
	
	private String title = "";
	private boolean closeable;
	private int slots;
	private ItemStack fillItem = null;
	
	public Menu(MenuListener l, boolean closeable, int slots)
	{
		this.l = l;
		this.closeable = closeable;
		this.setSlots(slots);
		
		id = idCnt++;
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setOption(int slot, ItemStack is)
	{
		if(slot < 0 || slot >= slots)
			slot = inv.firstEmpty();
		inv.setItem(slot, is);
	}
	
	public void setOption(int slot, Material m, String name)
	{
		if(m == null)
		{
			inv.setItem(slot, null);
			return;
		}
		ItemStack is = new ItemStack(m);
		Utils.setItemName(is, name);
		this.setOption(slot, is);
	}
	
	public void setEmptySlot(ItemStack is)
	{
		if(fillItem != null)
			inv.remove(fillItem.getType());
		fillItem = is;
		if(is == null)
			return;
		int i;
		while((i = inv.firstEmpty()) != -1)
			inv.setItem(i, is);
	}
	
	private void fillWithStrategy(int start, ArrayList<ItemStack> toFill, String strategy)
	{
		String mirrorStrategy = new StringBuilder(strategy).reverse().replace(0, 1, strategy).toString();
		for(int i = 0; i < mirrorStrategy.length(); i++)
			if(mirrorStrategy.charAt(i) == 'x')
				inv.setItem(start + i, toFill.remove(0));
	}
	
	public void order()
	{
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		for(ItemStack is : inv.getContents())
			if(is != null && (fillItem == null || !is.isSimilar(fillItem)))
				stacks.add(is);
		inv.clear();
		
		if(!stacks.isEmpty())
		{
			String fillStrategy;
			if(slots == 5)
			{
				switch(stacks.size())
				{
					case 1: fillStrategy = "--x"; break;
					case 2: fillStrategy = "x--"; break;
					case 3: fillStrategy = "x-x"; break;
					case 4: fillStrategy = "xx-"; break;
					default: fillStrategy = "xxx";
				}
				fillWithStrategy(0, stacks, fillStrategy);
			} else
			{
				int partSize = (int) Math.ceil(stacks.size() / (double) (slots/9));
				
				int avaible = stacks.size();
				int i = 0;
				while(avaible > 0)
				{
					int toAdd = Math.min(avaible, partSize);
					switch(toAdd)
					{
						case 1: fillStrategy = "----x"; break;
						case 2: fillStrategy = "x----"; break;
						case 3: fillStrategy = "x---x"; break;
						case 4: fillStrategy = "x-x--"; break;
						case 5: fillStrategy = "x-x-x"; break;
						case 6: fillStrategy = "x-xx-"; break;
						case 7: fillStrategy = "x-xxx"; break;
						case 8: fillStrategy = "xxxx-"; break;
						default: fillStrategy = "xxxxx";
					}
					fillWithStrategy(9*(i++), stacks, fillStrategy);
					avaible -= toAdd;
				}
			}
		}
		this.setEmptySlot(fillItem);
	}
	
	public void clearOptions()
	{
		for(int i = 0; i < slots; i++)
			inv.setItem(i, null);
		setEmptySlot(fillItem);
	}
	
	public void showMenu(Player pl)
	{
		pl.openInventory(inv);
	}
	
	public void setTitle(String title)
	{
		this.title = title;
		ItemStack[] is = inv.getContents();
		if(inv.getType() == InventoryType.CHEST)
			inv = Bukkit.createInventory(null, inv.getSize(), title);
		else
			inv = Bukkit.createInventory(null, inv.getType(), title);
		inv.setContents(is);
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setSlots(int slots)
	{
		if(slots < 5)
			slots = 5;
		else if(slots > 6 && slots % 9 != 0)
			slots = 9*(slots/9 + 1);
		if(slots == 5)
			inv = Bukkit.createInventory(null, InventoryType.HOPPER);
		else
			inv = Bukkit.createInventory(null, slots, title);
		this.slots = slots;
	}
	
	public void deregister()
	{
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e)
	{
		final HumanEntity p = e.getPlayer();
		if(!closeable && e.getInventory().equals(inv) && !canClose.remove(p))
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
				@Override
				public void run()
				{
					if(p instanceof Player)
						showMenu((Player) p);
				}
			});
	}
	
	@EventHandler
	public void onClick(final InventoryClickEvent e)
	{
		if(e.getInventory().equals(inv))
		{
			e.setCancelled(true);
			if(e.getView().getItem(e.getRawSlot()) != null && e.getView().getItem(e.getRawSlot()).equals(inv.getItem(e.getSlot())) && !inv.getItem(e.getSlot()).equals(fillItem))
			{
				if(!closeable)
					canClose.add(e.getWhoClicked());
				e.getWhoClicked().closeInventory();
				
				Player pl;
				if(e.getWhoClicked() instanceof Player)
					pl = (Player) e.getWhoClicked();
				else
					pl = null;
				final Player p = pl;
				Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
					@Override
					public void run()
					{
						if(l != null)
							l.onItemClick(id, e.getSlot(), inv.getItem(e.getSlot()), p);
					}
				});
			}
		}
	}
}
