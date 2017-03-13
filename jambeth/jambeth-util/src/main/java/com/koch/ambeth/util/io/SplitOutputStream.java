package com.koch.ambeth.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class SplitOutputStream extends OutputStream
{
	private final OutputStream[] os;

	public SplitOutputStream(OutputStream... os)
	{
		this.os = os;
	}

	@Override
	public void write(int b) throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.flush();
		}
	}

	@Override
	public void close() throws IOException
	{
		for (OutputStream os : this.os)
		{
			os.close();
		}
	}
}
