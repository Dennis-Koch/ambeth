package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class SqlStartsWithOperator extends CaseSensitiveTwoPlaceOperator {
    @Override
    protected void preProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) {
        if (parameters != null) {
            nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
        } else {
            if (SqlEscapeHelper.escapeIfNecessary(this, nameToValueMap)) {
                querySB.append('\'');
            }
        }
    }

    @Override
    protected void postProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) {
        if (parameters != null) {
            nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
        } else {
            querySB.append('%');
            if (SqlEscapeHelper.unescapeIfNecessary(this, nameToValueMap)) {
                querySB.append('\'');
            }
        }
    }

    @Override
    protected void expandOperatorQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) {
        querySB.append(" LIKE ");
    }
}
