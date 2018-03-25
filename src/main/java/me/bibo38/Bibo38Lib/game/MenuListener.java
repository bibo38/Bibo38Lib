package me.bibo38.Bibo38Lib.game;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface MenuListener
{
	void onItemClick(int clickedSlot, ItemStack clickedItem, Player p);
}
