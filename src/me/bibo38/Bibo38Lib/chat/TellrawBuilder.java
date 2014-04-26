package me.bibo38.Bibo38Lib.chat;

import java.util.ArrayList;

import me.bibo38.Bibo38Lib.Utils;

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
		try
		{
			// ChatSerializer.a(this.toString())
			Object chatSerializer = Utils.getMCClass("ChatSerializer").getMethod("a", String.class).invoke(null, this.toString());
			// new PacketPlayOutChat(chatSerializer);
			Object packChat = Utils.getMCClass("PacketPlayOutChat").getConstructor(Utils.getMCClass("IChatBaseComponent")).newInstance(chatSerializer);
			for(Player akt : p)
			{
				// ((CraftPlayer) akt).getHandle()
				Object player = akt.getClass().getMethod("getHandle").invoke(akt);
				// player.playerConnection
				Object pConn = player.getClass().getField("playerConnection").get(player);
				// pConn.sendPacket(packChat);
				pConn.getClass().getMethod("sendPacket", Utils.getMCClass("Packet")).invoke(pConn, packChat);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
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
