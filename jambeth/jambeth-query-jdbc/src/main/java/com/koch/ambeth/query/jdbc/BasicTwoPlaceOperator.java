package com.koch.ambeth.query.jdbc;

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
import java.util.Map;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public abstract class BasicTwoPlaceOperator implements IOperator, IInitializingBean {
	@Override
	public void afterPropertiesSet() throws Throwable {
		// Intended blank
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	protected abstract boolean isRightValueNull(Map<Object, Object> nameToValueMap);

	protected abstract boolean isRightValueNullOrEmpty(Map<Object, Object> nameToValueMap);

	protected abstract Class<?> getLeftOperandFieldType();

	protected abstract Class<?> getLeftOperandFieldSubType();

	@SuppressWarnings("unchecked")
	protected List<Object> getRemainingLeftOperandHandle(Map<Object, Object> nameToValueMap) {
		return (List<Object>) nameToValueMap.get(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE);
	}

	@SuppressWarnings("unchecked")
	protected List<Object> getRemainingRightOperandHandle(Map<Object, Object> nameToValueMap) {
		return (List<Object>) nameToValueMap.get(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters) {
		boolean rightValueIsNull = isRightValueNull(nameToValueMap);

		Class<?> leftOperandFieldType = getLeftOperandFieldType();

		Object outerRemainingLeftOperandHandle = nameToValueMap
				.remove(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE);
		Object outerRemainingRightOperandHandle = nameToValueMap
				.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
		Object outerConsumeRightOperandHandle = nameToValueMap
				.remove(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE);

		preProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
		boolean loopRight = true;
		while (loopRight) {
			loopRight = true;

			processLeftOperandAspect(querySB, nameToValueMap, joinQuery, parameters);
			// loopLeft = getRemainingLeftOperandHandle(nameToValueMap) != null;
			expandOperatorQuery(querySB, nameToValueMap, rightValueIsNull);

			// if (loopLeft)
			// {
			// nameToValueMap.put(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE, value)
			// }
			processRightOperandAspect(querySB, nameToValueMap, joinQuery, leftOperandFieldType,
					parameters);
			loopRight = getRemainingRightOperandHandle(nameToValueMap) != null;

			if (loopRight) {
				querySB.append(" OR ");
			}
		}
		postProcessOperate(querySB, nameToValueMap, joinQuery, parameters);

		if (outerRemainingLeftOperandHandle != null) {
			nameToValueMap.put(QueryConstants.REMAINING_LEFT_OPERAND_HANDLE,
					outerRemainingLeftOperandHandle);
		}
		if (outerRemainingRightOperandHandle != null) {
			nameToValueMap.put(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE,
					outerRemainingRightOperandHandle);
		}
		if (outerConsumeRightOperandHandle != null) {
			nameToValueMap.put(QueryConstants.CONSUME_RIGHT_OPERAND_HANDLE,
					outerConsumeRightOperandHandle);
		}
	}

	protected void preProcessOperate(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		querySB.append('(');
	}

	protected void postProcessOperate(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		querySB.append(')');
	}

	protected void processLeftOperandAspect(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		preProcessLeftOperand(querySB, nameToValueMap, parameters);
		processLeftOperand(querySB, nameToValueMap, joinQuery, parameters);
		postProcessLeftOperand(querySB, nameToValueMap, parameters);
	}

	protected void processRightOperandAspect(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, Class<?> leftValueOperandType, IList<Object> parameters) {
		preProcessRightOperand(querySB, nameToValueMap, parameters);
		processRightOperand(querySB, nameToValueMap, joinQuery, leftValueOperandType, parameters);
		postProcessRightOperand(querySB, nameToValueMap, parameters);
	}

	protected abstract void processLeftOperand(IAppendable querySB,
			IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters);

	protected abstract void processRightOperand(IAppendable querySB,
			IMap<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftValueOperandType,
			IList<Object> parameters);

	protected void preProcessLeftOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			IList<Object> parameters) {
		// Intended blank
	}

	protected void postProcessLeftOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			IList<Object> parameters) {
		// Intended blank
	}

	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			IList<Object> parameters) {
		// Intended blank
	}

	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			IList<Object> parameters) {
		// Intended blank
	}

	protected abstract void expandOperatorQuery(IAppendable querySB,
			IMap<Object, Object> nameToValueMap, boolean rightValueIsNull);
}
