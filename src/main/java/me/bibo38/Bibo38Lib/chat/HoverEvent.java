package me.bibo38.Bibo38Lib.chat;

import org.bukkit.Achievement;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class HoverEvent
{
	private String value = "", action = "";
	private TellrawText parent;
	
	protected HoverEvent(TellrawText parent)
	{
		this.parent = parent;
	}
	
	public TellrawText showText(String s)
	{
		action = "show_text";
		value = s;
		return parent;
	}
	
	public TellrawText showItem(String item)
	{
		action = "show_item";
		value = item;
		return parent;
	}
	
	public TellrawText showItem(ItemStack is)
	{
		return this.showItem("{id:" + is.getType().toString().toLowerCase() + "}");
	}
	
	public TellrawText showAchievement(String name)
	{
		action = "show_achievement";
		value = name;
		return parent;
	}
	
	public TellrawText showEntity(LivingEntity ent)
	{
		action = "show_entity";
		value = "{id:" + ent.getEntityId() + ",name:" + ent.getCustomName() + ",type:" + ent.getType().toString().toLowerCase() + "}";
		return parent;
	}
	
	public TellrawText showAchievement(Achievement ach)
	{
		return this.showAchievement(ach.name());
	}
	
	@Override
	public String toString()
	{
		if(value.isEmpty() || action.isEmpty())
			return "";
		return "\"hoverEvent\":{\"action\":\"" + action + "\",\"value\":\"" + value + "\"}";
	}
}
