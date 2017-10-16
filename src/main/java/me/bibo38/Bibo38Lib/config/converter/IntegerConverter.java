package me.bibo38.Bibo38Lib.config.converter;

import me.bibo38.Bibo38Lib.config.Converter;

import java.util.Optional;

public class IntegerConverter implements Converter<Integer>
{
	public static final IntegerConverter INSTANCE = new IntegerConverter();

	private IntegerConverter()
	{
	}
	
	@Override
	public Optional<Integer> convertFrom(Object o)
	{
		if(o instanceof Integer || o.getClass() == int.class)
			return Optional.of((int) o);
		
		return Optional.empty();
	}
}
