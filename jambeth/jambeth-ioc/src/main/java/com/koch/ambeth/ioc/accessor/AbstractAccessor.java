package com.koch.ambeth.ioc.accessor;

/*-
 * #%L
 * jambeth-ioc
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


public abstract class AbstractAccessor {
    public abstract boolean canRead();

    public abstract boolean canWrite();

    public abstract Object getValue(Object obj, boolean allowNullEquivalentValue);

    public abstract Object getValue(Object obj);

    public abstract void setValue(Object obj, Object value);

    public int getIntValue(Object obj) {
        return ((Number) getValue(obj, true)).intValue();
    }

    public void setIntValue(Object obj, int value) {
        setValue(obj, Integer.valueOf(value));
    }
}
