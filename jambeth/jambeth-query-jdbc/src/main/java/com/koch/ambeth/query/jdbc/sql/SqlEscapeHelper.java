package com.koch.ambeth.query.jdbc.sql;

import java.util.Map;

import com.koch.ambeth.query.IOperand;

public final class SqlEscapeHelper
{
	public static final Object IS_ESCAPING_KEY = new Object();

	private SqlEscapeHelper()
	{
	}

	public static boolean escapeIfNecessary(IOperand self, Map<Object, Object> nameToValueMap)
	{
		Object escapingOperand = nameToValueMap.get(IS_ESCAPING_KEY);
		if (escapingOperand != null)
		{
			// Already in escaping state. Do nothing here
			return false;
		}
		nameToValueMap.put(IS_ESCAPING_KEY, self);
		return true;
	}

	public static boolean unescapeIfNecessary(IOperand self, Map<Object, Object> nameToValueMap)
	{
		Object escapingOperand = nameToValueMap.get(IS_ESCAPING_KEY);
		if (escapingOperand != self)
		{
			// No own escaping state active. Do nothing here
			return false;
		}
		nameToValueMap.put(IS_ESCAPING_KEY, null);
		return true;
	}
}
