package me.bibo38.Bibo38Lib.config;

public interface Converter<T>
{
	/**
	 * This method converts a given object into the desired type.
	 *
	 * @param cl The desired class for the input data
	 * @param data The input data from the configuration(Primitive, Map, Set)
	 * @return An object instance
	 */
	T deserialize(Class<? extends T> cl, Object data);

	/**
	 * This method convers a given object into a
	 * basic representation.
	 *
	 * @param elem Element to convert
	 * @return Serialized data (Primitives, Map, Set)
	 */
	Object serialize(T elem);
}
