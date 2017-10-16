package me.bibo38.Bibo38Lib.config;

import me.bibo38.Bibo38Lib.config.converter.BooleanConverter;
import me.bibo38.Bibo38Lib.config.converter.CharConverter;
import me.bibo38.Bibo38Lib.config.converter.IntegerConverter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class YamlOM
{
	private static final Yaml YAML;
	
	static
	{
		DumperOptions dumpOpt = new DumperOptions();
		dumpOpt.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumpOpt.setAllowUnicode(true);
		
		YAML = new Yaml(new CleanRepresenter(), dumpOpt);
	}
	
	private Object obj;
	private StreamProvider streamProvider;
	private Map<Class<?>, Converter<?>> converters = new HashMap<>();

	public YamlOM(Object obj, StreamProvider streamProvider)
	{
		this.obj = Objects.requireNonNull(obj);
		this.streamProvider = Objects.requireNonNull(streamProvider);

		// Default converters
		setConverter(char.class, CharConverter.INSTANCE);
		setConverter(Character.class, CharConverter.INSTANCE);
		setConverter(boolean.class, BooleanConverter.INSTANCE);
		setConverter(Boolean.class, BooleanConverter.INSTANCE);
		setConverter(int.class, IntegerConverter.INSTANCE);
		setConverter(Integer.class, IntegerConverter.INSTANCE);
	}

	public YamlOM(Object obj, File file)
	{
		this(obj, new FileStreamProvider(file));
	}
	
	public void setConverter(Class<?> cl, Converter<?> conv)
	{
		converters.put(Objects.requireNonNull(cl), Objects.requireNonNull(conv));
	}

	public void save()
	{
		String fileData = YAML.dump(obj);
		streamProvider.withWriter(writer -> writer.write(fileData));
	}

	private static void checkClassConfigurable(Class<?> cl)
	{
		if(!cl.isAnnotationPresent(Configurable.class))
			throw new IllegalArgumentException("Class " + cl.getCanonicalName() + " is not annotated " +
					"with the Configurable interface");
	}

	@SuppressWarnings("unchecked")
	private void loadMapIntoObject(Object goal, Map<String, Object> data)
	{
		checkClassConfigurable(goal.getClass());

		if(data == null)
			return;

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
		streamProvider.withReader(rd -> loadMapIntoObject(obj, YAML.loadAs(rd, Map.class)));
	}

	private static final class CleanRepresenter extends Representer
	{
		private CleanRepresenter()
		{
			Represent oldDefaultRepresenter = representers.get(null);
			representers.put(null, obj ->
			{
				checkClassConfigurable(obj.getClass());

				Node ret = oldDefaultRepresenter.representData(obj);
				ret.setTag(Tag.MAP);
				return ret;
			});
		}
	}
}
