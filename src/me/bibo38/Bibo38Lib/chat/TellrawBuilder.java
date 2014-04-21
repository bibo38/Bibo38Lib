package me.bibo38.Bibo38Lib.chat;

import java.util.ArrayList;

import net.minecraft.server.v1_7_R3.ChatSerializer;
import net.minecraft.server.v1_7_R3.PacketPlayOutChat;

import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TellrawBuilder
{
	private ArrayList<TellrawText> list = new ArrayList<TellrawText>();
	private String text = "";
	
	public TellrawText add()
	{
		TellrawText neu = new TellrawText(this);
		list.add(neu);
		return neu;
	}
	
	public void sendTo(Player... p)
	{
		PacketPlayOutChat c = new PacketPlayOutChat(ChatSerializer.a(this.toString()));
		for(Player akt : p)
			((CraftPlayer) akt).getHandle().playerConnection.sendPacket(c);
	}
	
	public TellrawBuilder setText(String text)
	{
		this.text = text;
		return this;
	}
	
	@Override
	public String toString()
	{
		String ret = "{\"text\":\"" + text + "\",\"extra\":[";
		for(TellrawText t : list)
			ret += t.toString() + ",";
		ret = ret.substring(0, ret.length() - 1) + "]}";
		
		return ret;
	}
}
