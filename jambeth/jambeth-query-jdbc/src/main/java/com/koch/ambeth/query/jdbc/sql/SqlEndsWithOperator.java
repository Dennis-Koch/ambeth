package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public class SqlEndsWithOperator extends CaseSensitiveTwoPlaceOperator {
    @Override
    protected void preProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters) {
        if (parameters != null) {
            nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
        } else {
            if (SqlEscapeHelper.escapeIfNecessary(this, nameToValueMap)) {
                querySB.append('\'');
            }
            querySB.append('%');
        }
    }

    @Override
    protected void postProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters) {
        if (parameters != null) {
            nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
        } else {
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
