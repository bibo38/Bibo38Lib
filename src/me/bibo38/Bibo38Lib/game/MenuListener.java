package me.bibo38.Bibo38Lib.game;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface MenuListener
{
	public void onItemClick(int id, int clickedSlot, ItemStack clickedItem, Player p);
}
