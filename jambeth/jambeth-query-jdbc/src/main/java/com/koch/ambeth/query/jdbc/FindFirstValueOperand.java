package com.koch.ambeth.query.jdbc;

/*-
 * #%L
 * jambeth-query-jdbc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class FindFirstValueOperand implements IValueOperand, IOperand {
    public static final String P_OPERANDS = "Operands";

    @Property
    protected IValueOperand[] operands;

    @LogInstance
    private ILogger log;

    @Override
    public Object getValue(Map<Object, Object> nameToValueMap) {
        var operands = this.operands;
        if (operands == null) {
            return null;
        }
        for (int i = 0; i < operands.length; i++) {
            var operand = operands[i];
            if (operand != null) {
                var value = operand.getValue(nameToValueMap);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        throw new IllegalStateException("expandQuery not implemented yet for FindFirstValueOperand");
    }
}
