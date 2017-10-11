package me.bibo38.Bibo38Lib.config;

import java.util.Optional;

public interface Converter<T>
{
	/**
	 * This method tries to convert a given object into the desired type.
	 * 
	 * @param o The object to convert
	 * @return Optional.empty if the conversion is not possible, otherwise the converted value
	 */
	Optional<T> convertFrom(Object o);
}
