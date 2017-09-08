package me.bibo38.Bibo38Lib.chat;

public class ClickEvent
{
	private String value = "", action = "";
	private TellrawText parent;
	
	protected ClickEvent(TellrawText parent)
	{
		this.parent = parent;
	}
	
	public TellrawText runCommand(String cmd)
	{
		action = "run_command";
		value = cmd;
		return parent;
	}
	
	public TellrawText suggestCommand(String cmd)
	{
		action = "suggest_command";
		value = cmd;
		return parent;
	}
	
	public TellrawText openURL(String url)
	{
		action = "open_url";
		value = url;
		return parent;
	}
	
	@Override
	public String toString()
	{
		if(value.isEmpty() || action.isEmpty())
			return "";
		return "\"clickEvent\":{\"action\":\"" + action + "\",\"value\":\"" + value + "\"}";
	}
}
