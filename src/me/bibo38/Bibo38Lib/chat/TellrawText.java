package me.bibo38.Bibo38Lib.chat;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;

public class TellrawText
{
	private static final int FORMAT_MASK = 31;
	
	private ChatColor color = null;
	private int formatting = 0;
	private TellrawBuilder parent;
	private ClickEvent click;
	private HoverEvent hover;
	private String text = "\"text\":\"\"";
	
	public TellrawText(TellrawBuilder parent)
	{
		this.parent = parent;
		this.hover = new HoverEvent(this);
		this.click = new ClickEvent(this);
	}
	
	public TellrawText setColor(ChatColor col)
	{
		color = col;
		return this;
	}
	
	public TellrawText setFormatting(int format)
	{
		formatting = format & FORMAT_MASK;
		return this;
	}
	
	public TellrawText setText(String text)
	{
		this.text = "\"text\":\"" + text + "\"";
		return this;
	}
	
	public TellrawText setTranslate(String translate)
	{
		this.text = "\"translate\":\"" + translate + "\"";
		return this;
	}
	
	public TellrawText setScore(Score s)
	{
		this.text = "\"score\":{\"name\":\"" + s.getEntry() + "\",\"objective\":\"" + s.getObjective().getName() + "\"}";
		return this;
	}
	
	public ClickEvent onClick()
	{
		return click;
	}
	
	public HoverEvent onHover()
	{
		return hover;
	}
	
	public TellrawBuilder finish()
	{
		return parent;
	}
	
	@Override
	public String toString()
	{
		String ret = "{" + text + ",";
		if(color != null)
			ret += "\"color\":\"" + color.name().toLowerCase() + "\",";
		if((formatting & Formatting.BOLD) != 0)
			ret += "\"bold\":\"true\",";
		if((formatting & Formatting.ITALIC) != 0)
			ret += "\"italic\":\"true\",";
		if((formatting & Formatting.UNDERLINED) != 0)
			ret += "\"underlined\":\"true\",";
		if((formatting & Formatting.STRIKETHROUGH) != 0)
			ret += "\"strikethrough\":\"true\",";
		if((formatting & Formatting.OBFUSCATED) != 0)
			ret += "\"obfuscated\":\"true\",";
		ret += hover.toString() + (hover.toString().isEmpty()? "" : ",");
		ret += click.toString() + (click.toString().isEmpty()? "" : ",");
		return ret.substring(0, ret.length() - 1) + "}";
	}
}
