package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.util.appendable.IAppendable;

import java.util.Map;

public class SqlIsNotEqualToOperator extends CaseSensitiveTwoPlaceOperator {
    @Override
    protected void expandOperatorQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) {
        if (rightValueIsNull) {
            querySB.append(" IS NOT ");
        } else {
            querySB.append("<>");
        }
    }
}
