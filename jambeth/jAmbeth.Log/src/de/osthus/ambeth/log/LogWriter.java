package de.osthus.ambeth.log;

import java.io.IOException;
import java.io.Writer;

public class LogWriter extends Writer
{
	protected final ILogger log;

	protected final StringBuilder sb = new StringBuilder();

	public LogWriter(ILogger log)
	{
		this.log = log;

	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		sb.append(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		// Intended blank
	}

	@Override
	public void close() throws IOException
	{
		log.info(sb.toString());
	}
}
