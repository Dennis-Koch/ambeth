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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlStartsWithOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
		{
			nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
		}
		else
		{
			if (SqlEscapeHelper.escapeIfNecessary(this, nameToValueMap))
			{
				querySB.append('\'');
			}
		}
	}

	@Override
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
		{
			nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
		}
		else
		{
			querySB.append('%');
			if (SqlEscapeHelper.unescapeIfNecessary(this, nameToValueMap))
			{
				querySB.append('\'');
			}
		}
	}

	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		querySB.append(" LIKE ");
	}
}
