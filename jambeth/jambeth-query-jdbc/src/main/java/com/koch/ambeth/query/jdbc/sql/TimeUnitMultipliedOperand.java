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

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;

import java.util.List;
import java.util.Map;

public class TimeUnitMultipliedOperand implements IOperand {
    @Property
    protected IOperand timeUnit;

    @Property
    protected IOperand multiplicatedInterval;

    @Override
    public void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) {
        querySB.append('(');
        timeUnit.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
        querySB.append(")::interval * (");
        multiplicatedInterval.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
        querySB.append(')');
    }
}
