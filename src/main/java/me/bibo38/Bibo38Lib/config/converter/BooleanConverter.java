package me.bibo38.Bibo38Lib.config.converter;

import me.bibo38.Bibo38Lib.config.Converter;

import java.util.Optional;

public final class BooleanConverter implements Converter<Boolean>
{
	public static final BooleanConverter INSTANCE = new BooleanConverter();
	
	private BooleanConverter()
	{
	}
	
	@Override
	public Optional<Boolean> convertFrom(Object o)
	{
		if(o instanceof Boolean || o.getClass() == boolean.class)
			return Optional.of((boolean) o);
		
		return Optional.empty();
	}
}
