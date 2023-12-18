package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class SqlIsInOperator extends CaseSensitiveTwoPlaceOperator {
    @Override
    public void operate(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        if (isRightValueNullOrEmpty(nameToValueMap)) {
            // No-op
            querySB.append("0=1");
            return;
        }
        super.operate(querySB, nameToValueMap, joinQuery, parameters);
    }

    @Override
    protected boolean supportsMultiValueOperand() {
        return true;
    }

    @Override
    protected void expandOperatorQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) {
        Class<?> leftOperandFieldType = getLeftOperandFieldType();
        if (!java.sql.Array.class.isAssignableFrom(leftOperandFieldType)) {
            connectionDialect.appendIsInOperatorClause(querySB);
            return;
        }
        querySB.append(" INTERSECT ");
    }

    @Override
    protected void preProcessOperate(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        Class<?> leftOperandFieldType = getLeftOperandFieldType();
        if (java.sql.Array.class.isAssignableFrom(leftOperandFieldType)) {
            querySB.append("EXISTS");
        }
        super.preProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
    }

    @Override
    protected void preProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) {
        querySB.append('(');
    }

    @Override
    protected void postProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) {
        querySB.append(')');
    }
}
