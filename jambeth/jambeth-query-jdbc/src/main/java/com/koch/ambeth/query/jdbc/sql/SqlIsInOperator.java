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
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlIsInOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		if (isRightValueNullOrEmpty(nameToValueMap))
		{
			// No-op
			querySB.append("0=1");
			return;
		}
		super.operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	protected boolean supportsMultiValueOperand()
	{
		return true;
	}

	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		Class<?> leftOperandFieldType = getLeftOperandFieldType();
		if (!java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			connectionDialect.appendIsInOperatorClause(querySB);
			return;
		}
		querySB.append(" INTERSECT ");
	}

	@Override
	protected void preProcessOperate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Class<?> leftOperandFieldType = getLeftOperandFieldType();
		if (java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			querySB.append("EXISTS");
		}
		super.preProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		querySB.append('(');
	}

	@Override
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		querySB.append(')');
	}
}
