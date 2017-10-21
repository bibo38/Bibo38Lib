package me.bibo38.Bibo38Lib.config.converter;

import me.bibo38.Bibo38Lib.config.Converter;
import me.bibo38.Bibo38Lib.config.utility.ChatMessage;
import org.bukkit.ChatColor;

public class ChatMessageConverter implements Converter<ChatMessage>
{
	private static final char CONFIG_COLOR_CHAR = '&';

	@Override
	public ChatMessage deserialize(Class<? extends ChatMessage> cl, Object data)
	{
		if(!String.class.equals(data.getClass()))
			return null;

		return new ChatMessage(((String) data).replace(CONFIG_COLOR_CHAR, ChatColor.COLOR_CHAR));
	}

	@Override
	public Object serialize(ChatMessage msg)
	{
		return msg.toString().replace(ChatColor.COLOR_CHAR, CONFIG_COLOR_CHAR);
	}
}
