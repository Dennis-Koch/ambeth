package de.osthus.ambeth.shell.core;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.IConversionHelper;

/**
 *
 * @author daniel.mueller
 *
 */
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
			possibleKey = possibleKey.substring(1);
			return get(possibleKey);
		}
		return possibleKey;
	}

	@Override
	public String filter(String input)
	{
		for (Object key : variables.keySet())
		{
			Object value = variables.get(key);
			if (value != null)
			{
				input = input.replaceAll("\\" + VAR_MARKER + key, value.toString());
			}
		}
		return input;
	}
}
