package de.osthus.ambeth.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

public class CompositeReader extends Reader
{
	private final Reader[] readers;

	private int readerIndex = 0;

	public CompositeReader(Reader... readers)
	{
		this.readers = readers;
	}

	public CompositeReader(Collection<? extends Reader> readers)
	{
		this.readers = readers.toArray(new Reader[readers.size()]);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		if (readerIndex >= readers.length)
		{
			return -1;
		}
		int bytesRead = readers[readerIndex].read(cbuf, off, len);
		if (bytesRead == -1)
		{
			readerIndex++;
			return 0;
		}
		return bytesRead;
	}

	@Override
	public void close() throws IOException
	{
		for (Reader reader : readers)
		{
			reader.close();
		}
	}
}
