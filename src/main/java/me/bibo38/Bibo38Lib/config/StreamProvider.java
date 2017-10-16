package me.bibo38.Bibo38Lib.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Used to provide Character Streams
 */
public interface StreamProvider
{
	/**
	 * @param operation A callback, which executes the logic for the {@link Writer}
	 */
	void withWriter(IOOperation<Writer> operation);

	/**
	 * @param operation A callback, which executes the logic for the {@link Reader}
	 */
	void withReader(IOOperation<Reader> operation);

	@FunctionalInterface
	interface IOOperation<T>
	{
		void execute(T obj) throws IOException;
	}
}
