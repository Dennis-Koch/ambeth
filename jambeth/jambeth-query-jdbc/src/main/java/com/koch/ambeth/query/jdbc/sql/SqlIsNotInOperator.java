package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public class SqlIsNotInOperator extends CaseSensitiveTwoPlaceOperator {
    @Override
    protected void preProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters) {
        querySB.append('(');
    }

    @Override
    protected void postProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters) {
        querySB.append(')');
    }

    @Override
    protected boolean supportsMultiValueOperand() {
        return true;
    }

    @Override
    protected void expandOperatorQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) {
        querySB.append(" NOT IN ");
    }
}
