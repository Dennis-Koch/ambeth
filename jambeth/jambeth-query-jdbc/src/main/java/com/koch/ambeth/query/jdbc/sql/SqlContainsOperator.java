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

public class SqlContainsOperator extends SqlLikeOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
		{
			return;
		}
		super.preProcessRightOperand(querySB, nameToValueMap, null);
		querySB.append('%');
	}

	@Override
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
		{
			return;
		}
		querySB.append('%');
		super.postProcessRightOperand(querySB, nameToValueMap, null);
	}

	@Override
	protected void processRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
			IList<Object> parameters)
	{
		nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
		nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
		super.processRightOperand(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
		nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
		nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
	}
}
