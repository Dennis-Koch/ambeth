package com.koch.ambeth.filter;

/*-
 * #%L
 * jambeth-persistence-test
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
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class OperandDummy implements IOperator {
	private String type;

	private Map<String, ?> attributes;

	private IOperand[] operands;

	OperandDummy(String type, IOperand... operands) {
		this.type = type;
		this.operands = operands;
	}

	OperandDummy(String type, Map<String, ?> attributes) {
		this.type = type;
		this.attributes = attributes;
	}

	OperandDummy(String type, Map<String, ?> attributes, IOperand... operands) {
		this.type = type;
		this.attributes = attributes;
		this.operands = operands;
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		throw new IllegalStateException("Not implemented");
	}

	public String getType() {
		return type;
	}

	public Map<String, ?> getAttributes() {
		return attributes;
	}

	public IOperand[] getOperands() {
		return operands;
	}
}
