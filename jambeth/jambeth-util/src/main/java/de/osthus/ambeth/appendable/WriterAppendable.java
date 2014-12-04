package de.osthus.ambeth.appendable;

import java.io.IOException;
import java.io.Writer;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class WriterAppendable implements IAppendable
{
	protected final Writer target;

	public WriterAppendable(Writer target)
	{
		this.target = target;
	}

	@Override
	public IAppendable append(char value)
	{
		try
		{
			target.append(value);
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
			target.append(value);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return this;
	}

}
