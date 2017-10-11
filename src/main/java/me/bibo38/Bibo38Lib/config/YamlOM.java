package me.bibo38.Bibo38Lib.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import me.bibo38.Bibo38Lib.config.converter.BooleanConverter;
import me.bibo38.Bibo38Lib.config.converter.CharConverter;

public class YamlOM<T>
{
	private static final Yaml YAML;
	
	static
	{
		DumperOptions dumpOpt = new DumperOptions();
		dumpOpt.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumpOpt.setAllowUnicode(true);
		
		YAML = new Yaml(new CleanRepresenter(), dumpOpt);
	}
	
	private T obj;
	private File file;
	private Map<Class<?>, Converter<?>> converters = new HashMap<>();

	public YamlOM(T obj, File file)
	{
		this.obj = Objects.requireNonNull(obj);
		this.file = Objects.requireNonNull(file);
		
		try
		{
			file.createNewFile();
		} catch (IOException e)
		{
			throw new RuntimeException("Cannot create file " + file, e);
		}
		
		if(!file.canWrite())
			throw new IllegalArgumentException("File '" + file + "' cannot be written!");
		
		
		// Default converters
		setConverter(char.class, CharConverter.INSTANCE);
		setConverter(Character.class, CharConverter.INSTANCE);
		setConverter(boolean.class, BooleanConverter.INSTANCE);
		setConverter(Boolean.class, BooleanConverter.INSTANCE);
	}
	
	public void setConverter(Class<?> cl, Converter<?> conv)
	{
		converters.put(Objects.requireNonNull(cl), Objects.requireNonNull(conv));
	}

	public void save()
	{
		String fileData = YAML.dump(obj);

		try(FileWriter writer = new FileWriter(file))
		{
			writer.write(fileData);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadMapIntoObject(Object goal, Map<String, Object> data)
	{
		for(ConfigurableField f : ConfigurableField.fromClass(goal.getClass()))
		{
			if(!data.containsKey(f.getName()))
				continue;
			
			Object value = data.get(f.getName());
			if(f.getType().isAssignableFrom(value.getClass()))
				f.setValue(goal, value);
			else if(value instanceof Map)
				loadMapIntoObject(f.getValue(goal), (Map<String, Object>) value);
			else
			{
				Optional<?> convertedValue = Optional.empty();
				if(converters.containsKey(f.getType()))
					convertedValue = converters.get(f.getType()).convertFrom(value);
				
				f.setValue(goal, convertedValue.orElseThrow(() -> new RuntimeException("Cannot convert a value " + value + " of type " + value.getClass() + " into a field of the type " + f.getType())));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void load()
	{
		try(Reader rd = new FileReader(file))
		{
			loadMapIntoObject(obj, YAML.loadAs(rd, Map.class));
			
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static final class CleanRepresenter extends Representer
	{
		private CleanRepresenter()
		{
			Represent oldDefaultRepresenter = representers.get(null);
			representers.put(null, obj ->
			{
				Node ret = oldDefaultRepresenter.representData(obj);
				if(obj.getClass().isAnnotationPresent(Configurable.class))
					ret.setTag(Tag.MAP);
				return ret;
			});
		}
	}
}
