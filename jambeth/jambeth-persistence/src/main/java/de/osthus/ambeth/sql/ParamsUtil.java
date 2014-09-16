package de.osthus.ambeth.sql;

import java.util.List;

public final class ParamsUtil
{
	public static void addParam(List<Object> parameters, Object value)
	{
		parameters.add(value);
	}

	public static void addParams(List<Object> parameters, List<Object> values)
	{
		for (int i = 0, size = values.size(); i < size; i++)
		{
			parameters.add(values.get(i));
		}
	}

	private ParamsUtil()
	{
		// Intended blank
	}
}
