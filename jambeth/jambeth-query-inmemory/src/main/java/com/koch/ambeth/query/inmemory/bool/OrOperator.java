package com.koch.ambeth.query.inmemory.bool;

/*-
 * #%L
 * jambeth-query-inmemory
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
import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryBooleanOperand;
import com.koch.ambeth.util.collections.IMap;

public class OrOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryBooleanOperand[] operands;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		for (IInMemoryBooleanOperand operand : operands)
		{
			Boolean value = operand.evaluate(nameToValueMap);
			if (value != null && value.booleanValue())
			{
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
