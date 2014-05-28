package de.osthus.ambeth.xml.simple;

import java.io.IOException;

import sun.nio.cs.StreamEncoder;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.xml.IAppendable;

public class AppendableStreamEncoder implements IAppendable
{
	protected final StreamEncoder se;

	public AppendableStreamEncoder(StreamEncoder se)
	{
		this.se = se;
	}

	@Override
	public IAppendable append(char value)
	{
		try
		{
			se.append(value);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return this;
	}

	@Override
	public IAppendable append(CharSequence value)
	{
		try
		{
			se.append(value);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return this;
	}
}
