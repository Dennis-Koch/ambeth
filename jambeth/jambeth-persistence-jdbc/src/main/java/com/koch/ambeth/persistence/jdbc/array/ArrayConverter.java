package com.koch.ambeth.persistence.jdbc.array;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import lombok.SneakyThrows;

import java.sql.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class ArrayConverter implements IDedicatedConverter {
    @Autowired
    protected IConnectionExtension connectionExtension;

    @Autowired
    protected IConversionHelper conversionHelper;

    @SneakyThrows
    @Override
    public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) {
        if (Array.class.isAssignableFrom(sourceType)) {
            var array = (Array) value;

            var list = new ArrayList<>();
            try (var rs = array.getResultSet()) {
                Class<?> componentType = null;
                if (expectedType.isArray()) {
                    componentType = expectedType.getComponentType();
                } else if (Collection.class.isAssignableFrom(expectedType) && additionalInformation != null) {
                    componentType = (Class<?>) additionalInformation;
                }
                while (rs.next()) {
                    var index = ((Number) rs.getObject(1)).intValue();
                    var item = rs.getObject(2);
                    while (list.size() < index) {
                        list.add(null);
                    }
                    item = conversionHelper.convertValueToType(componentType, item);
                    list.set(index - 1, item);
                }
                if (expectedType.isArray()) {
                    var targetArray = java.lang.reflect.Array.newInstance(componentType, list.size());
                    var preparedArraySet = com.koch.ambeth.util.Arrays.prepareSet(targetArray);
                    for (int a = 0, size = list.size(); a < size; a++) {
                        preparedArraySet.set(a, list.get(a));
                    }
                    return targetArray;
                } else if (Set.class.isAssignableFrom(expectedType)) {
                    var result = new java.util.HashSet<>((int) ((list.size() + 1) / 0.75f), 0.75f);
                    result.addAll(list);
                    return result;
                } else if (Collection.class.isAssignableFrom(expectedType)) {
                    var result = new java.util.ArrayList<>(list.size());
                    result.addAll(list);
                    return result;
                }
            }
        } else if (sourceType.isArray()) {
            if (Array.class.isAssignableFrom(expectedType)) {
                ParamChecker.assertParamNotNull(additionalInformation, "additionalInformation");
                var componentType = (Class<?>) additionalInformation;
                return connectionExtension.createJDBCArray(componentType, value);
            } else if (Set.class.isAssignableFrom(expectedType)) {
                var result = new java.util.HashSet<>(Arrays.asList(value));
                return result;
            }
        } else if (Collection.class.isAssignableFrom(sourceType)) {
            if (Array.class.isAssignableFrom(expectedType)) {
                ParamChecker.assertParamNotNull(additionalInformation, "additionalInformation");

                var valueArray = ((Collection<?>) value).toArray();
                var componentType = (Class<?>) additionalInformation;
                return connectionExtension.createJDBCArray(componentType, valueArray);
            }
        }
        throw new IllegalArgumentException(
                "Cannot convert from '" + sourceType + "' to '" + expectedType + "'. This is a bug if I get called for types which I do not support and I did not register with!");
    }
}
