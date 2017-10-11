package me.bibo38.Bibo38Lib.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration specific Abstraction of Reflections.
 * 
 * It's main goal is to abstract the specific handling
 * of the class fields away from the specific configuration
 * implementation. It handles the setting and getting of
 * field values through setters and getters.
 * Furthermore it ignores transient fields.
 */
public class ConfigurableField
{
	private Field field;
	private Optional<Method> setter;
	private Optional<Method> getter;
	
	private ConfigurableField(Field field)
	{
		this.field = Objects.requireNonNull(field);
		field.setAccessible(true);
		
		String suffix = firstCharToUppercase(field.getName());
		
		Class<?> cl = field.getDeclaringClass();
		try
		{
			setter = Optional.of(cl.getDeclaredMethod("set" + suffix, field.getType()));
		} catch (NoSuchMethodException e)
		{
			setter = Optional.empty();
		}
		
		try
		{
			Method getter = cl.getDeclaredMethod("get" + suffix);
			if(getter.getReturnType() == field.getType())
				this.getter = Optional.of(getter);
			else
				this.getter = Optional.empty();	
		} catch (NoSuchMethodException e)
		{
			this.getter = Optional.empty();
		}
		
		setter.ifPresent(m -> m.setAccessible(true));
		getter.ifPresent(m -> m.setAccessible(true));
	}
	
	public void setValue(Object o, Object value)
	{
		try
		{
			if(setter.isPresent())
			{
				setter.get().invoke(o, value);
			} else
			{
				field.set(o, value);
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			throw new RuntimeException("Error setting the value of the field " + field.getName(), e);
		}
	}
	
	public Object getValue(Object o)
	{
		try
		{
			if(getter.isPresent())
			{
				return getter.get().invoke(o);
			} else
			{
				return field.get(o);
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			throw new RuntimeException("Error getting the value of the field " + field.getName(), e);
		}
	}
	
	public String getName()
	{
		return field.getName();
	}
	
	public Class<?> getType()
	{
		return field.getType();
	}
	
	private static String firstCharToUppercase(String input)
	{
		if(input.length() == 0)
			return input;
		
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}
	
	private static boolean isFieldConfigurable(Field f)
	{
		int modifier = f.getModifiers();
		if(Modifier.isTransient(modifier))
			return false;
		
		if(Modifier.isStatic(modifier))
			return false;
		
		return true;
	}
	
	public static List<ConfigurableField> fromClass(Class<?> cl)
	{
		LinkedList<ConfigurableField> ret = new LinkedList<>();
		
		for(Field f : cl.getDeclaredFields())
		{
			if(!isFieldConfigurable(f))
				continue;
			
			ret.add(new ConfigurableField(f));
		}
		
		return ret;
	}
}
