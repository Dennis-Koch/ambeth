package com.koch.ambeth.shell.core.resulttype;

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
