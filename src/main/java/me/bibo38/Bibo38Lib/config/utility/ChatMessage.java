package me.bibo38.Bibo38Lib.config.utility;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Objects;

public class ChatMessage
{
	private String message;

	public ChatMessage(String message)
	{
		this.message = Objects.requireNonNull(message);
	}

	public ChatMessage(Object... parts)
	{
		this(Arrays.stream(parts).map(Object::toString).reduce(String::concat).orElse(""));
	}

	public void sendTo(CommandSender to, Object... args)
	{
		to.sendMessage(format(args));
	}

	public void broadcast(Object... args)
	{
		Bukkit.broadcastMessage(format(args));
	}

	public String format(Object... args)
	{
		return String.format(message, args);
	}

	@Override
	public String toString()
	{
		return message;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		ChatMessage that = (ChatMessage) o;
		return message.equals(that.message);
	}

	@Override
	public int hashCode()
	{
		return message.hashCode();
	}
}
