package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public class SqlLikeOperator extends CaseSensitiveTwoPlaceOperator {
    @Override
    protected void preProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters) {
        if (parameters != null) {
            // Intended blank
        } else {
            if (SqlEscapeHelper.escapeIfNecessary(this, nameToValueMap)) {
                querySB.append('\'');
            }
        }
    }

    @Override
    protected void postProcessRightOperand(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters) {
        if (parameters != null) {
            // Intended blank
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

    @Override
    protected void postProcessOperate(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        querySB.append(" ESCAPE '\\'");
        super.postProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
    }
}
