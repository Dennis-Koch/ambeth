package com.koch.ambeth.query.inmemory.ordinal;

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
import com.koch.ambeth.query.inmemory.IInMemoryNumericOperand;
import com.koch.ambeth.util.collections.IMap;

public abstract class AbstractBinaryOrdinalOperator extends AbstractOperator
		implements IInMemoryBooleanOperand {
	@Property
	protected IInMemoryNumericOperand leftOperand;

	@Property
	protected IInMemoryNumericOperand rightOperand;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap) {
		Double leftValue = leftOperand.evaluateNumber(nameToValueMap);
		if (leftValue == null) {
			return null;
		}
		Double rightValue = rightOperand.evaluateNumber(nameToValueMap);
		if (rightValue == null) {
			return null;
		}
		return evaluateIntern(leftValue.doubleValue(), rightValue.doubleValue());
	}

	protected abstract Boolean evaluateIntern(double leftValue, double rightValue);
}
