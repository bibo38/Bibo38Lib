package me.bibo38.Bibo38Lib.config;

import me.bibo38.Bibo38Lib.config.converter.ConfigurationSerializableConverter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class YamlOM
{
	private Yaml yaml;
	private Object obj;
	private StreamProvider streamProvider;
	private Map<Class<?>, Converter<?>> converters = new HashMap<>();

	private Map<Class<?>, Optional<Converter<?>>> converterCache = new HashMap<>();

	public YamlOM(Object obj, StreamProvider streamProvider)
	{
		this.obj = Objects.requireNonNull(obj);
		this.streamProvider = Objects.requireNonNull(streamProvider);

		// Default converters
		setConverter(ConfigurationSerializable.class, new ConfigurationSerializableConverter());

		// YAML initialisation
		DumperOptions dumpOpt = new DumperOptions();
		dumpOpt.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumpOpt.setAllowUnicode(true);

		yaml = new Yaml(new SafeConstructor(), new CleanRepresenter(), dumpOpt);
		yaml.setBeanAccess(BeanAccess.FIELD);
	}

	public YamlOM(Object obj, File file)
	{
		this(obj, new FileStreamProvider(file));
	}
	
	public <T> void setConverter(Class<T> cl, Converter<T> conv)
	{
		converters.put(Objects.requireNonNull(cl), Objects.requireNonNull(conv));
		converterCache.clear(); // Because converters are mosly added at the start, this is faster than only clearing Optional.empty values
	}

	public void save()
	{
		String fileData = yaml.dump(obj);
		streamProvider.withWriter(writer -> writer.write(fileData));
	}

	private void checkClassConfigurable(Class<?> cl)
	{
		if(getConverter(cl).isPresent())
			return;

		if(!cl.isAnnotationPresent(Configurable.class))
			throw new IllegalArgumentException("Class " + cl.getCanonicalName() + " is not annotated " +
					"with the Configurable interface");
	}

	private void copyObjectData(Object goal, Object source)
	{
		checkClassConfigurable(goal.getClass());

		if(source == null)
			return;

		for(ConfigurableField f : ConfigurableField.fromClass(goal.getClass()))
		{
			Object value = f.getValue(source);
			if(value != null && f.getType().isAnnotationPresent(Configurable.class))
				copyObjectData(f.getValue(goal), value);
			else
				f.setValue(goal, value);
		}
	}
	
	public void load()
	{
		streamProvider.withReader(rd -> copyObjectData(obj, yaml.loadAs(rd, obj.getClass())));
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<Converter<T>> getConverter(Class<? extends T> cl)
	{
		Optional<Converter<?>> ret = converterCache.get(cl);
		if(ret != null)
			return (Optional) ret;

		for(Map.Entry<Class<?>, Converter<?>> conv : converters.entrySet())
		{
			if(conv.getKey().isAssignableFrom(cl))
			{
				ret = Optional.of(conv.getValue());
				converterCache.put(cl, ret);
				return (Optional) ret;
			}
		}

		converterCache.put(cl, Optional.empty());
		return Optional.empty();
	}

	private final class SafeConstructor extends Constructor
	{
		private SafeConstructor()
		{
			yamlClassConstructors.put(NodeId.mapping, new MyMappingConstructor());
		}

		@Override
		protected Class<?> getClassForName(String name) throws ClassNotFoundException
		{
			Class<?> cl = getClass().getClassLoader().loadClass(name);
			checkClassConfigurable(cl);
			return cl;
		}

		private class MyMappingConstructor extends ConstructMapping
		{
			@SuppressWarnings("unchecked")
			@Override
			protected Object createEmptyJavaBean(MappingNode node)
			{
				return getConverter(node.getType())
						.map(conv -> conv.deserialize((Class) node.getType(), constructMapping(node)))
						.orElseGet(() -> super.createEmptyJavaBean(node));
			}

			@Override
			protected Object constructJavaBean2ndStep(MappingNode node, Object object)
			{
				if(getConverter(node.getType()).isPresent())
					return object; // Already constructed in the empty phase

				return super.constructJavaBean2ndStep(node, object);
			}
		}
	}

	private final class CleanRepresenter extends Representer
	{
		@SuppressWarnings("unchecked")
		private CleanRepresenter()
		{
			Represent oldDefaultRepresenter = representers.get(null);
			representers.put(null, obj ->
				getConverter(obj.getClass())
					.map(conv -> represent(((Converter) conv).serialize(obj)))
					.orElseGet(() ->
					{
						checkClassConfigurable(obj.getClass());

						Node ret = oldDefaultRepresenter.representData(obj);
						ret.setTag(Tag.MAP);
						return ret;
					})
			);
		}
	}
}
