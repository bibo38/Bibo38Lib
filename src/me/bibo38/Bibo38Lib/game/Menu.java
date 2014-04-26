package me.bibo38.Bibo38Lib.game;

import java.util.HashSet;

import me.bibo38.Bibo38Lib.Startfunc;
import me.bibo38.Bibo38Lib.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
	
	public Menu(MenuListener l, boolean closeable, int slots)
	{
		this.l = l;
		this.closeable = closeable;
		this.slots = slots;
		if(slots % 9 != 0 && slots != 5)
			throw new IllegalArgumentException("Illegal Slot amount: "+slots);
		id = idCnt++;
		
		if(slots == 5)
			inv = Bukkit.createInventory(null, InventoryType.HOPPER);
		else
			inv = Bukkit.createInventory(null, slots, title);
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setOption(int slot, Material m, String name)
	{
		if(m == null)
		{
			inv.setItem(slot, null);
			return;
		}
		ItemStack is = new ItemStack(m);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		inv.setItem(slot, is);
	}
	
	public void showMenu(Player pl)
	{
		if(slots != 5)
		{
			pl.openInventory(inv);
			return;
		}
		
		try
		{
			// EntityPlayer p = ((CraftPlayer) pl).getHandle();
			Object p = pl.getClass().getMethod("getHandle").invoke(pl);
			Class<?> pCl = p.getClass();
			
			// p.playerConnection
			Object pConn = pCl.getField("playerConnection").get(p);
			if (pConn == null)
				return;
			
			// p.nextContainerCounter()
			int contianerCnt = (int) pCl.getMethod("nextContainerCounter").invoke(p);
			// new CraftContainer(inv, pl, containerCnt)
			Object container = Utils.getCBClass("inventory.CraftContainer").getConstructor(Inventory.class, HumanEntity.class, int.class).newInstance(inv, pl, contianerCnt);
	
		    /* container = CraftEventFactory.callInventoryOpenEvent(p, container);
		    if (container == null)
		    	return; */
			
		    // container.windowId
		    int windowId = container.getClass().getField("windowId").getInt(container);
		    // new PacketPlayOutOpenWindow(windowId, 9, title, inv.getSize(), true)
		    Object packOpenWindow = Utils.getMCClass("PacketPlayOutOpenWindow").getConstructor(int.class, int.class, String.class, int.class, boolean.class).newInstance(windowId, 9, title, inv.getSize(), true);
		    // pConn.sendPacket();
		    pConn.getClass().getMethod("sendPacket", Utils.getMCClass("Packet")).invoke(pConn, packOpenWindow);
		    // p.activeContainer = container;
		    pCl.getField("activeContainer").set(p, container);
		    // container.addSlotListener(p);
		    container.getClass().getMethod("addSlotListener", Utils.getMCClass("ICrafting")).invoke(container, p);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setTitle(String title)
	{
		this.title = title;
		if(slots != 5) // Kein Hopper
		{
			ItemStack[] is = inv.getContents();
			inv = Bukkit.createInventory(null, inv.getSize(), title);
			inv.addItem(is);
		}
	}
	
	public String getTitle()
	{
		return title;
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
	public void onClick(InventoryClickEvent e)
	{
		if(e.getInventory().equals(inv))
		{
			e.setCancelled(true);
			if(e.getView().getItem(e.getRawSlot()) != null && e.getView().getItem(e.getRawSlot()).equals(inv.getItem(e.getSlot())) && inv.getItem(e.getSlot()) != null)
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
				final int slot = e.getSlot();
				Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
					@Override
					public void run()
					{
						if(l != null)
							l.onItemClick(id, slot, p);
					}
				});
			}
		}
	}
}
