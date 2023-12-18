package com.koch.ambeth.util.config;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.model.INotifyPropertyChanged;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public interface IProperties extends INotifyPropertyChanged {
    IProperties getParent();

    boolean containsKey(String key);

    <T> T get(String key);

    <T> T get(String key, IProperties initiallyCalledProps);

    /**
     * Resolve all known properties within a given value string. This will also be done recursively
     * for properties which contain other properties.<br>
     * Unknown variables will stay in the original string without further modification (to later put a
     * string and resolve it)<br>
     * Examples: {@link com.koch.ambeth.config.PropertyResolverTest}<br>
     * "exampleString with var ${exampleVar} more content ${${recursivelyResolvedVar}} etc..."
     *
     * @param value
     * @return
     */
    String resolvePropertyParts(CharSequence value);

    String getString(String key);

    String getString(String key, String defaultValue);

    Iterator<Entry<String, Object>> iterator();

    ISet<String> collectAllPropertyKeys();

    void collectAllPropertyKeys(Set<String> allPropertiesSet);
}
