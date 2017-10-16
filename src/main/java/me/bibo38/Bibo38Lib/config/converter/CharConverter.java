package me.bibo38.Bibo38Lib.config.converter;

import me.bibo38.Bibo38Lib.config.Converter;

import java.util.Optional;

public final class CharConverter implements Converter<Character>
{
	public static final CharConverter INSTANCE = new CharConverter();
	
	private CharConverter()
	{
	}
	
	@Override
	public Optional<Character> convertFrom(Object o)
	{
		if(o instanceof String)
		{
			String str = (String) o;
			if(str.length() == 1)
				return Optional.of(str.charAt(0));
		}
		
		return Optional.empty();
	}	
}
