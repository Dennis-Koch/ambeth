package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public class SqlAllOperand implements IOperand {
    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        querySB.append("1=1");
    }
}
