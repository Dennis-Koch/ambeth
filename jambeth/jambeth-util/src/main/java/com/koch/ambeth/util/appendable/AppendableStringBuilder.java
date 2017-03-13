package com.koch.ambeth.util.appendable;

public class AppendableStringBuilder implements IAppendable, CharSequence
{
	protected final StringBuilder sb;

	public AppendableStringBuilder()
	{
		this(new StringBuilder());
	}

	public AppendableStringBuilder(StringBuilder sb)
	{
		this.sb = sb;
	}

	public void reset()
	{
		sb.setLength(0);
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

	@Override
	public char charAt(int index)
	{
		return sb.charAt(index);
	}

	@Override
	public int length()
	{
		return sb.length();
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return sb.subSequence(start, end);
	}

	@Override
	public String toString()
	{
		return sb.toString();
	}
}
