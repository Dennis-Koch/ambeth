package com.koch.ambeth.shell.core.resulttype;

/**
 * A class for a kind of command return type which contains only simple information
 */
public class SingleResult extends CommandResult
{
	private StringBuffer value = new StringBuffer();

	public SingleResult(String value)
	{
		this.value.append(value);
	}

	public SingleResult()
	{
	}

	public String getValue()
	{
		return value.toString();
	}

	public void setValue(String value)
	{
		this.value.append(value);
	}

	public void addValue(String string)
	{
		value.append(string);
	}

	@Override
	public String toString()
	{
		String value = getValue();
		if (!value.endsWith(System.lineSeparator()))
		{
			value = value + System.lineSeparator();
		}
		return value;
	}
}
