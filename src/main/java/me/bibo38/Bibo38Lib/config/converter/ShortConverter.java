package me.bibo38.Bibo38Lib.config.converter;

import me.bibo38.Bibo38Lib.config.Converter;

import java.util.Optional;

public final class ShortConverter implements Converter<Short>
{
	public static final ShortConverter INSTANCE = new ShortConverter();

	private ShortConverter()
	{
	}

	@Override
	public Optional<Short> convertFrom(Object o)
	{
		if(o instanceof Short || o.getClass() == short.class)
			return Optional.of((short) o);

		if(o instanceof Integer || o.getClass() == int.class)
		{
			int value = (int) o;
			if(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
				return Optional.of((short) value);
		}

		return Optional.empty();
	}
}
