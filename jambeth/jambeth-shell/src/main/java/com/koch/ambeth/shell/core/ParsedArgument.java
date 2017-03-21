package com.koch.ambeth.shell.core;

/*-
 * #%L
 * jambeth-shell
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.shell.core.annotation.CommandArg;

/**
 *
 */
public class ParsedArgument
{

	protected static final Pattern userInputPattern = Pattern.compile("([^=]+)=(.*)"); // captures the left side of the first "=" character as well as the right

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
		Matcher matcher = userInputPattern.matcher(userInput);
		if (matcher.matches())
		{
			name = matcher.group(1);
			value = shellContext.resolve(unquoteString(matcher.group(2)));
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
