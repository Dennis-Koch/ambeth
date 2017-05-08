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

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class OverlapsOperand implements IOperand {
	@Property
	protected IOperand leftOperand;

	@Property
	protected IOperand rightOperand;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		querySB.append('(');
		leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(" OVERLAPS ");
		rightOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(')');
	}
}
