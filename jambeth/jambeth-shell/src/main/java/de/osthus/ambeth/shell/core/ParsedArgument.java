package de.osthus.ambeth.shell.core;

import de.osthus.ambeth.shell.core.annotation.CommandArg;

/**
 * 
 */
public class ParsedArgument
{

	protected final String userInput;
	protected final int index;

	protected String name;
	protected Object value;
	protected ShellContext shellContext;

	public ParsedArgument(String userInput, int index, ShellContext shellContext)
	{
		this.userInput = userInput;
		this.index = index;
		this.shellContext = shellContext;
		this.parse();
	}

	private void parse()
	{
		if (userInput.contains("="))
		{
			String[] nvPair = userInput.split("=");
			name = nvPair[0];
			value = shellContext.resolve(unquoteString(nvPair[1]));
		}
		else
		{
			name = "";
			value = shellContext.resolve(unquoteString(userInput));
		}
	}

	/**
	 * remove leading and trailing quotes
	 * 
	 * @param s
	 * @return
	 */
	private String unquoteString(String s)
	{
		if (s.matches("\".*\""))
		{
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	public boolean matchedBy(CommandArg ca)
	{
		if (!ca.name().isEmpty())
		{
			return name.matches("(?i:" + ca.name() + ")");
		}
		else
		{
			return name.isEmpty();
		}
	}
}
