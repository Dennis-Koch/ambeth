package com.koch.ambeth.query.jdbc.sql;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public class DifferenceOperand implements IOperand, IInitializingBean {
    @Property
    protected IOperand[] operands;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertTrue(operands.length != 0, "Operands");
    }

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        if (operands.length > 1) {
            querySB.append('(');
        }
        operands[0].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
        for (int a = 1, size = operands.length; a < size; a++) {
            querySB.append('-');
            operands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
        }
        if (operands.length > 1) {
            querySB.append(')');
        }
    }
}
