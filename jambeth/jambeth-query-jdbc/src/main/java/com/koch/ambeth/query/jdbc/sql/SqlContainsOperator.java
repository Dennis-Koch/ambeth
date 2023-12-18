package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class SqlContainsOperator extends SqlLikeOperator {
    @Override
    protected void preProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) {
        if (parameters != null) {
            return;
        }
        super.preProcessRightOperand(querySB, nameToValueMap, null);
        querySB.append('%');
    }

    @Override
    protected void postProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) {
        if (parameters != null) {
            return;
        }
        querySB.append('%');
        super.postProcessRightOperand(querySB, nameToValueMap, null);
    }

    @Override
    protected void processRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType, List<Object> parameters) {
        nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
        nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
        super.processRightOperand(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
        nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
        nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
    }
}
