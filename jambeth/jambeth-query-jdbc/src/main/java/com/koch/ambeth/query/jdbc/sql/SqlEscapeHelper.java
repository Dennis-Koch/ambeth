package com.koch.ambeth.query.jdbc.sql;

/*-
 * #%L
 * jambeth-query-jdbc
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
