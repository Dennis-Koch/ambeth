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

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class SqlAdditionalSelectOperand implements IOperator, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IOperand column;

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(column, "column");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setColumn(IOperand column)
	{
		this.column = column;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		List<String> additionalSelectColumnList = (List<String>) nameToValueMap.get(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		if (additionalSelectColumnList == null)
		{
			return;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
		try
		{
			column.expandQuery(sb, nameToValueMap, joinQuery, parameters);
			additionalSelectColumnList.add(sb.toString());
			if (querySB != null)
			{
				column.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}
}
