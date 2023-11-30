package com.koch.ambeth.query.inmemory;

/*-
 * #%L
 * jambeth-query-inmemory
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
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public abstract class AbstractOperator implements IOperator, IInitializingBean {
    @Override
    public void afterPropertiesSet() throws Throwable {
        // Intended blank
    }

    @Override
    public final void expandQuery(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void operate(IAppendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
        throw new UnsupportedOperationException();
    }
}
