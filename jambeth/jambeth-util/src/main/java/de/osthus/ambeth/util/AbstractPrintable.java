package de.osthus.ambeth.util;

public abstract class AbstractPrintable implements IPrintable
{
	protected IPrintable p;

	public AbstractPrintable(IPrintable p)
	{
		this.p = p;
	}

	@Override
	public abstract void toString(StringBuilder sb);
}
