package de.osthus.esmeralda.misc;

import java.io.IOException;
import java.io.Writer;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class EsmeraldaWriter implements IWriter
{
	protected final Writer writer;

	public EsmeraldaWriter(Writer writer)
	{
		this.writer = writer;
	}

	@Override
	public IWriter append(char c)
	{
		try
		{
			writer.append(c);
			return this;
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IWriter append(CharSequence csq)
	{
		try
		{
			writer.append(csq);
			return this;
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IWriter append(CharSequence csq, int start, int end)
	{
		try
		{
			writer.append(csq, start, end);
			return this;
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public String toString()
	{
		return writer.toString();
	}
}
