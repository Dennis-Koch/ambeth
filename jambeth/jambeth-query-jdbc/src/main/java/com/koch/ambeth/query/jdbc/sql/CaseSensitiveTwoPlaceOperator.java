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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.persistence.sql.ParamsUtil;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.jdbc.TwoPlaceOperator;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

abstract public class CaseSensitiveTwoPlaceOperator extends TwoPlaceOperator {
    @Property(defaultValue = "true")
    protected boolean caseSensitive;

    @Autowired
    protected IConnectionDialect connectionDialect;

    protected int maxInClauseBatchThreshold;

    @Autowired
    protected ListToSqlUtil listToSqlUtil;

    @Autowired
    protected IPersistenceHelper persistenceHelper;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        maxInClauseBatchThreshold = connectionDialect.getMaxInClauseBatchThreshold();
    }

    @Override
    protected void processLeftOperandAspect(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        boolean caseSensitive = this.caseSensitive;
        Class<?> leftOperandFieldType = getLeftOperandFieldType();
        if (supportsMultiValueOperand() && java.sql.Array.class.isAssignableFrom(leftOperandFieldType)) {
            querySB.append("SELECT ");

            if (!caseSensitive) {
                querySB.append("LOWER(");
            }
            querySB.append("COLUMN_VALUE");
            if (!caseSensitive) {
                querySB.append(") COLUMN_VALUE");
            }
            querySB.append(" FROM TABLE(");
            leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
            querySB.append(")");
            return;
        }
        if (!caseSensitive) {
            querySB.append("LOWER(");
        }
        super.processLeftOperandAspect(querySB, nameToValueMap, joinQuery, parameters);
        if (!caseSensitive) {
            querySB.append(')');
        }
    }

    protected boolean supportsMultiValueOperand() {
        return false;
    }

    @Override
    protected void processRightOperandAspect(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType, List<Object> parameters) {
        if (supportsMultiValueOperand() && rightOperand instanceof IMultiValueOperand) {
            preProcessRightOperand(querySB, nameToValueMap, parameters);
            handleMultiValueOperand((IMultiValueOperand) rightOperand, querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
            postProcessRightOperand(querySB, nameToValueMap, parameters);
            return;
        }
        if (!caseSensitive) {
            querySB.append("LOWER(");
        }
        super.processRightOperandAspect(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
        if (!caseSensitive) {
            querySB.append(')');
        }
    }

    protected void handleMultiValueOperand(IMultiValueOperand operand, IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
            List<Object> parameters) {
        var splitValues = (List<List<Object>>) nameToValueMap.get(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
        if (parameters == null) {
            if (splitValues != null) {
                throw new IllegalStateException("Must never happen");
            }
            if (!caseSensitive) {
                // 'Dirty' hack. This is invalid SQL but we need this only for a paging query-key
                querySB.append("LOWER(");
            }
            var values = operand.getMultiValue(nameToValueMap);
            listToSqlUtil.expandValue(querySB, values, this, nameToValueMap);
            if (!caseSensitive) {
                querySB.append(')');
            }
            return;
        }
        if (splitValues == null) {
            var values = operand.getMultiValue(nameToValueMap);
            if (connectionDialect.isCompactMultiValueRecommended(values)) {
                splitValues = persistenceHelper.splitValues(values, maxInClauseBatchThreshold);
                handleWithMultiValueLeftField(querySB, nameToValueMap, parameters, splitValues);
                return;
            }
            splitValues = persistenceHelper.splitValues(values);
        }

        if (!java.sql.Array.class.isAssignableFrom(leftOperandFieldType)) {
            handleWithSingleValueLeftField(querySB, nameToValueMap, parameters, splitValues);
        } else {
            handleWithMultiValueLeftField(querySB, nameToValueMap, parameters, splitValues);
        }
    }

    protected void handleWithSingleValueLeftField(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters, List<List<Object>> splitValues) {
        if (splitValues.isEmpty()) {
            return;
        }

        String placeholder;
        if (caseSensitive) {
            placeholder = "?";
        } else {
            placeholder = "LOWER(?)";
        }

        var values = splitValues.get(0);
        for (int i = 0, size = values.size(); i < size; i++) {
            if (i != 0) {
                querySB.append(",");
            }
            querySB.append(placeholder);
            ParamsUtil.addParam(parameters, values.get(i));
        }

        if (splitValues.size() > 1) {
            splitValues.remove(0);
            nameToValueMap.put(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE, splitValues);
        } else {
            nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
        }
    }

    protected void handleWithMultiValueLeftField(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters, List<List<Object>> splitValues) {
        Class<?> leftOperandFieldType = getLeftOperandFieldSubType();
        connectionDialect.handleWithMultiValueLeftField(querySB, nameToValueMap, parameters, splitValues, caseSensitive, leftOperandFieldType);
        nameToValueMap.remove(QueryConstants.REMAINING_RIGHT_OPERAND_HANDLE);
    }
}
