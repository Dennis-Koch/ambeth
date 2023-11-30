package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.util.appendable.IAppendable;

import java.util.Map;

public class SqlNotLikeOperator extends SqlLikeOperator {
    @Override
    protected void expandOperatorQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) {
        querySB.append(" NOT LIKE ");
    }
}
