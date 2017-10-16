package me.bibo38.Bibo38Lib.test.config;

import me.bibo38.Bibo38Lib.config.StreamProvider;

import java.io.*;
import java.util.Objects;

public class StringStreamProvider implements StreamProvider
{
	public String input;
	public String output;

	public StringStreamProvider(String input)
	{
		this.input = Objects.requireNonNull(input);
	}

	@Override
	public void withWriter(IOOperation<Writer> operation)
	{
		StringWriter writer = new StringWriter();
		try
		{
			operation.execute(writer);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		output = writer.getBuffer().toString();
	}

	@Override
	public void withReader(IOOperation<Reader> operation)
	{
		try
		{
			operation.execute(new StringReader(input));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
