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

import java.util.Map;

import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperatorAwareOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.query.jdbc.sql.SqlColumnOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public abstract class TwoPlaceOperator extends BasicTwoPlaceOperator {
	protected IOperand leftOperand;

	protected IOperand rightOperand;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(leftOperand, "leftOperand");
		ParamChecker.assertNotNull(rightOperand, "rightOperand");
	}

	public void setLeftOperand(IOperand leftOperand) {
		this.leftOperand = leftOperand;
	}

	public void setRightOperand(IOperand rightOperand) {
		this.rightOperand = rightOperand;
	}

	@Override
	protected void processLeftOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters) {
		leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	protected void processRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, Class<?> leftValueOperandType, IList<Object> parameters) {
		Object existingHint = nameToValueMap.put(QueryConstants.EXPECTED_TYPE_HINT,
				leftValueOperandType);
		try {
			rightOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		finally {
			if (existingHint != null) {
				nameToValueMap.put(QueryConstants.EXPECTED_TYPE_HINT, existingHint);
			}
			else {
				nameToValueMap.remove(QueryConstants.EXPECTED_TYPE_HINT);
			}
		}
	}

	@Override
	protected Class<?> getLeftOperandFieldType() {
		IOperand leftOperand = this.leftOperand;
		if (leftOperand instanceof SqlColumnOperand) {
			return ((SqlColumnOperand) leftOperand).getColumnType();
		}
		return null;
	}

	@Override
	protected Class<?> getLeftOperandFieldSubType() {
		IOperand leftOperand = this.leftOperand;
		if (leftOperand instanceof SqlColumnOperand) {
			return ((SqlColumnOperand) leftOperand).getColumnSubType();
		}
		return null;
	}

	@Override
	protected boolean isRightValueNull(Map<Object, Object> nameToValueMap) {
		IOperand rightOperand = this.rightOperand;
		if (rightOperand instanceof IValueOperand) {
			return ((IValueOperand) rightOperand).getValue(nameToValueMap) == null;
		}
		else if (rightOperand instanceof IMultiValueOperand) {
			return ((IMultiValueOperand) rightOperand).isNull(nameToValueMap);
		}
		return false;
	}

	@Override
	protected boolean isRightValueNullOrEmpty(Map<Object, Object> nameToValueMap) {
		IOperand rightOperand = this.rightOperand;
		if (rightOperand instanceof IMultiValueOperand) {
			return ((IMultiValueOperand) rightOperand).isNullOrEmpty(nameToValueMap);
		}
		else if (rightOperand instanceof IValueOperand) {
			Object value = ((IValueOperand) rightOperand).getValue(nameToValueMap);
			return value == null || "".equals(value);
		}
		return false;
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters) {
		IOperand leftOperand = this.leftOperand;
		IOperand rightOperand = this.rightOperand;
		if (leftOperand instanceof IOperatorAwareOperand) {
			((IOperatorAwareOperand) leftOperand).operatorStart(nameToValueMap);
		}
		if (rightOperand instanceof IOperatorAwareOperand) {
			((IOperatorAwareOperand) rightOperand).operatorStart(nameToValueMap);
		}
		try {
			super.operate(querySB, nameToValueMap, joinQuery, parameters);
		}
		finally {
			if (leftOperand instanceof IOperatorAwareOperand) {
				((IOperatorAwareOperand) leftOperand).operatorEnd(nameToValueMap);
			}
			if (rightOperand instanceof IOperatorAwareOperand) {
				((IOperatorAwareOperand) rightOperand).operatorEnd(nameToValueMap);
			}
		}
	}
}
