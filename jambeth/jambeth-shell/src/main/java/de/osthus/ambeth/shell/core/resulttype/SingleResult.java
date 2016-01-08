package de.osthus.ambeth.shell.core.resulttype;

/**
 * A class for a kind of command return type which contains only simple information
 */
public class SingleResult extends CommandResult
{
	private String name;

	private StringBuffer value = new StringBuffer();

	public SingleResult(String name)
	{
		this.name = name;
	}

	public SingleResult()
	{
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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
