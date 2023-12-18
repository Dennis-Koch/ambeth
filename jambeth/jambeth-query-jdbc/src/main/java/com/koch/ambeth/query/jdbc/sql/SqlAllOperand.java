package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class SqlAllOperand implements IOperand {
    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        querySB.append("1=1");
    }
}
