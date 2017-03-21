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

import org.apache.commons.lang3.StringEscapeUtils;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;

public class ShellContextImpl implements ShellContext, IStartingBean
{
	protected HashMap<String, Object> variables = new HashMap<String, Object>();

	@Autowired
	protected IConversionHelper conversionHelper;

	/**
	 * load all ambeth properties into shell context
	 *
	 */
	@Override
	public void afterStarted() throws Throwable
	{
		Properties application = Properties.getApplication();
		for (String key : application.collectAllPropertyKeys())
		{
			variables.put(key, application.get(key));
		}
	}

	/**
	 *
	 */
	@Override
	public void set(String key, Object value)
	{
		variables.put(key, value);
	}

	/**
	 *
	 */
	@Override
	public Object get(String key)
	{
		return variables.get(key);
	}

	@Override
	public <T> T get(String key, Class<T> expectedType)
	{
		return conversionHelper.convertValueToType(expectedType, get(key));
	}

	/**
	 *
	 */
	@Override
	public <T> T get(String key, T defaultValue)
	{
		@SuppressWarnings("unchecked")
		T value = (T) get(key, defaultValue.getClass());
		if (value == null)
		{
			value = defaultValue;
			set(key, defaultValue);
		}
		return value;
	}

	@Override
	public void remove(String key)
	{
		variables.remove(key);
	}

	@Override
	public IMap<String, Object> getAll()
	{
		return variables;
	}

	@Override
	public Object resolve(String possibleKey)
	{
		if (possibleKey.startsWith(VAR_MARKER))
		{
			Object ret = get(possibleKey.substring(1));
			if (ret != null)
			{
				return ret;
			}
		}
		return possibleKey;
	}

	@Override
	public String filter(String input)
	{
		for (Object key : variables.keySet())
		{
			Object value = variables.get(key);
			if (value != null && key instanceof String)
			{
				String escapeJava = StringEscapeUtils.escapeJava((String) key);
				input = input.replaceAll("\\" + VAR_MARKER + escapeJava, Matcher.quoteReplacement(value.toString()));
			}
		}
		return input;
	}

}
