package me.bibo38.Bibo38Lib.game;

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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Menu implements Listener
{
	public boolean closeOnSelect = true;

	private Inventory inv;
	private HashSet<HumanEntity> canClose = new HashSet<HumanEntity>();
	private MenuListener l;
	
	private String title = "";
	private boolean closeable = true;
	private int slots;
	private ItemStack fillItem = null;
	private transient Plugin plugin;
	
	
	public Menu(MenuListener l, Plugin plugin)
	{
		this.l = Objects.requireNonNull(l);
		this.plugin = Objects.requireNonNull(plugin);

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		setSlots(1);
	}

	public void setOption(int slot, ItemStack is)
	{
		if(slot < 0)
			throw new IllegalArgumentException("Negative Slots cannot be set: Tried " + slot);

		if(slot >= inv.getSize())
			setSlots(slot + 1);

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
					case 1: fillStrategy = "--x";
						break;
					case 2: fillStrategy = "x--";
						break;
					case 3: fillStrategy = "x-x";
						break;
					case 4: fillStrategy = "xx-";
						break;
					default: fillStrategy = "xxx";
				}
				fillWithStrategy(0, stacks, fillStrategy);
			} else
			{
				int partSize = (int) Math.ceil(stacks.size() / (double) (slots / 9));
				
				int avaible = stacks.size();
				int i = 0;
				while(avaible > 0)
				{
					int toAdd = Math.min(avaible, partSize);
					switch(toAdd)
					{
						case 1: fillStrategy = "----x";
							break;
						case 2: fillStrategy = "x----";
							break;
						case 3: fillStrategy = "x---x";
							break;
						case 4: fillStrategy = "x-x--";
							break;
						case 5: fillStrategy = "x-x-x";
							break;
						case 6: fillStrategy = "x-xx-";
							break;
						case 7: fillStrategy = "x-xxx";
							break;
						case 8: fillStrategy = "xxxx-";
							break;
						default: fillStrategy = "xxxxx";
					}
					fillWithStrategy(9 * (i++), stacks, fillStrategy);
					avaible -= toAdd;
				}
			}
		}
		this.setEmptySlot(fillItem);
	}
	
	public void clearOptions()
	{
		inv.clear();
		setEmptySlot(fillItem);
	}
	
	public void showMenu(Player pl)
	{
		pl.openInventory(inv);
	}
	
	public void setTitle(String title)
	{
		this.title = title;
		recreateInventory();
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setSlots(int slots)
	{
		if(slots <= 5)
			slots = 5;
		else if(slots % 9 != 0)
			slots = 9 * (slots / 9 + 1);

		this.slots = slots;
		recreateInventory();
	}

	private void recreateInventory()
	{
		ItemStack is[] = (inv != null) ? inv.getContents() : new ItemStack[0];

		assert slots == 5 || (slots % 9 == 0) : "Wrong Slot size";
		if(slots == 5)
			inv = Bukkit.createInventory(null, InventoryType.HOPPER, title);
		else
			inv = Bukkit.createInventory(null, slots, title);
		inv.setContents(is);
	}
	
	public void deregister()
	{
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e)
	{
		if(closeable || !e.getInventory().equals(inv))
			return;

		final HumanEntity p = e.getPlayer();
		if(p instanceof Player && !canClose.remove(p))
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				showMenu((Player) p);
			});
		}
	}
	
	@EventHandler
	public void onClick(final InventoryClickEvent e)
	{
		if(!e.getInventory().equals(inv))
			return;

		if (!(e.getWhoClicked() instanceof Player))
			return;
		Player p = (Player) e.getWhoClicked();

		e.setCancelled(true);

		ItemStack clickedItem = e.getView().getItem(e.getRawSlot());
		ItemStack inventoryItem = inv.getItem(e.getSlot());
		if(clickedItem == null || !clickedItem.equals(inventoryItem) || fillItem.equals(inventoryItem))
			return;

		if (!closeable)
			canClose.add(p);

		if (closeOnSelect)
			p.closeInventory();

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			l.onItemClick(e.getSlot(), inv.getItem(e.getSlot()), p);
		});
	}
}
