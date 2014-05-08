package de.osthus.ambeth.xml.simple;

import de.osthus.ambeth.xml.IAppendable;

public class AppendableStringBuilder implements IAppendable
{
	protected final StringBuilder sb;

	public AppendableStringBuilder(StringBuilder sb)
	{
		this.sb = sb;
	}
	
	@Override
	public IAppendable append(char value)
	{
		sb.append(value);
		return this;
	}
	
	@Override
	public IAppendable append(CharSequence value)
	{
		sb.append(value);
		return this;
	}
}
