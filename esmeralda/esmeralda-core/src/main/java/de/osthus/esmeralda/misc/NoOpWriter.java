package de.osthus.esmeralda.misc;

import java.io.IOException;
import java.io.Writer;

public class NoOpWriter extends Writer
{
	@Override
	public Writer append(char c)
	{
		return this;
	}

	@Override
	public Writer append(CharSequence csq)
	{
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end)
	{
		return this;
	}

	@Override
	public String toString()
	{
		return "<n/a>";
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		// intended blank
	}

	@Override
	public void flush() throws IOException
	{
		// intended blank
	}

	@Override
	public void close() throws IOException
	{
		// intended blank
	}
}
