package de.osthus.ambeth.sql;

import java.util.List;
import java.util.Map;

public final class ParamsUtil
{
	public static void addParam(Map<Integer, Object> params, Object value)
	{
		params.put(Integer.valueOf(params.size() + 1), value);
	}

	public static void addParams(Map<Integer, Object> params, List<Object> values)
	{
		for (int i = 0, size = values.size(); i < size; i++)
		{
			params.put(Integer.valueOf(params.size() + 1), values.get(i));
		}
	}

	private ParamsUtil()
	{
		// Intended blank
	}
}
