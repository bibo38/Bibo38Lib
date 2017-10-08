package me.bibo38.Bibo38Lib.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.Objects;

public class YamlOM<T>
{
	private static final Yaml YAML = new Yaml(new SafeConstructor(), new CleanRepresenter());

	private Class<T> cl;
	private File file;

	public YamlOM(Class<T> cl, File file)
	{
		this.cl = Objects.requireNonNull(cl);
		this.file = Objects.requireNonNull(file);

		if(!file.canWrite())
			throw new IllegalArgumentException("File '" + file + "' cannot be written!");
	}

	public void saveToYaml(T obj)
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

	public T load()
	{
		try(Reader rd = new FileReader(file))
		{
			return YAML.loadAs(rd, cl);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private static class SafeConstructor extends Constructor
	{
		@Override
		protected Class<?> getClassForName(String name) throws ClassNotFoundException
		{
			Class<?> cl = getClass().getClassLoader().loadClass(name);

			if(cl.isAnnotationPresent(Configurable.class))
				return cl;

			throw new RuntimeException("Not allowed");
		}
	}

	private static final class CleanRepresenter extends Representer
	{
		private CleanRepresenter()
		{
			setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

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
