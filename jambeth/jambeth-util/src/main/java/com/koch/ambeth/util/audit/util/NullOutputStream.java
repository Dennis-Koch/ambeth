package com.koch.ambeth.util.audit.util;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream
{
	public static final NullOutputStream INSTANCE = new NullOutputStream();

	public NullOutputStream()
	{
		// Intended blank
	}

	@Override
	public void write(int b) throws IOException
	{
		// Intended blank
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		// Intended blank
	}

	@Override
	public void write(byte[] b, int offset, int len) throws IOException
	{
		// Intended blank
	}
}
