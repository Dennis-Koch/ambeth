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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlFunctionOperand implements IOperand, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String name;

	protected IOperand[] operands;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(operands, "operands");
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOperands(IOperand[] operands) {
		this.operands = operands;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		Object existingHint = nameToValueMap.get(QueryConstants.EXPECTED_TYPE_HINT);
		if (existingHint != null) {
			nameToValueMap.put(QueryConstants.EXPECTED_TYPE_HINT, null);
		}
		try {
			querySB.append(name).append('(');

			boolean notFirst = false;
			for (int i = 0; i < operands.length; i++) {
				IOperand operand = operands[i];
				if (notFirst) {
					querySB.append(',');
				}
				notFirst = true;
				operand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
			querySB.append(')');
		}
		finally {
			if (existingHint != null) {
				nameToValueMap.put(QueryConstants.EXPECTED_TYPE_HINT, existingHint);
			}
		}
	}
}
