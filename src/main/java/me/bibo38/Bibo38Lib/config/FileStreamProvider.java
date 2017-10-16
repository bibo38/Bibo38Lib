package me.bibo38.Bibo38Lib.config;

import java.io.*;
import java.util.Objects;

public class FileStreamProvider implements StreamProvider
{
	private File file;

	public FileStreamProvider(File file)
	{
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

	@Override
	public void withWriter(IOOperation<Writer> operation)
	{
		try(FileWriter writer = new FileWriter(file))
		{
			operation.execute(writer);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void withReader(IOOperation<Reader> operation)
	{
		try(Reader rd = new FileReader(file))
		{
			operation.execute(rd);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
