package de.osthus.esmeralda.handler.uni.expr;

import javax.lang.model.element.Name;

public class MockName implements Name
{
	private final String value;

	public MockName(String value)
	{
		this.value = value;
	}

	@Override
	public int length()
	{
		return value.length();
	}

	@Override
	public char charAt(int index)
	{
		return value.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return value.subSequence(start, end);
	}

	@Override
	public boolean contentEquals(CharSequence cs)
	{
		return value.contentEquals(cs);
	}

	@Override
	public String toString()
	{
		return value;
	}
}
