package me.bibo38.Bibo38Lib.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
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
	private static final Yaml YAML;
	
	static
	{
		DumperOptions dumpOpt = new DumperOptions();
		dumpOpt.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumpOpt.setAllowUnicode(true);
		
		YAML = new Yaml(new SafeConstructor(), new CleanRepresenter(), dumpOpt);
	}
	
	private T obj;
	private File file;

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
	public T load()
	{
		try(Reader rd = new FileReader(file))
		{
			return YAML.loadAs(rd, (Class<T>) obj.getClass());
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