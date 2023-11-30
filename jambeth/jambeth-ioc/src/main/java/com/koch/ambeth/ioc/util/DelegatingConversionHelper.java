package com.koch.ambeth.ioc.util;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.IPreparedConverter;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.lang.reflect.Array;

public class DelegatingConversionHelper extends IConversionHelper implements IInitializingBean, IDedicatedConverterExtendable {
    protected final ClassTupleExtendableContainer<IDedicatedConverter> converters = new ClassTupleExtendableContainer<>("dedicatedConverter", "type", true);

    protected IConversionHelper defaultConversionHelper;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(defaultConversionHelper, "defaultConversionHelper");
    }

    public void setDefaultConversionHelper(IConversionHelper defaultConversionHelper) {
        this.defaultConversionHelper = defaultConversionHelper;
    }

    @Override
    public <T> T convertValueToType(Class<T> expectedType, Object value) {
        return convertValueToType(expectedType, value, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convertValueToType(Class<T> expectedType, Object value, Object additionalInformation) {
        if (value == null || expectedType == null || expectedType.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        if (expectedType.isPrimitive() && value instanceof Number) {
            return defaultConversionHelper.convertValueToType(expectedType, value);
        }
        return convertDefault(expectedType, value, additionalInformation);
    }

    protected <T> T convertDefault(Class<T> expectedType, Object value, Object additionalInformation) {
        var sourceValue = value;
        while (true) {
            var sourceClass = sourceValue.getClass();
            var dedicatedConverter = converters.getExtension(sourceClass, expectedType);
            if (dedicatedConverter == null) {
                break;
            }
            Object targetValue;
            try {
                targetValue = dedicatedConverter.convertValueToType(expectedType, sourceClass, sourceValue, additionalInformation);
            } catch (Throwable e) {
                throw RuntimeExceptionUtil.mask(e, "Error occured while converting value: " + sourceValue);
            }
            if (targetValue == null) {
                if (expectedType.isPrimitive()) {
                    throw new IllegalStateException(
                            "It is not allowed that an instance of " + IDedicatedConverter.class.getName() + " returns null like " + dedicatedConverter + " did for conversion from '" +
                                    sourceClass.getName() + "' to '" + expectedType + "'");
                }
                return null;
            }
            if (expectedType.isAssignableFrom(targetValue.getClass())) {
                return (T) targetValue;
            }
            if (targetValue.getClass().equals(sourceValue.getClass())) {
                throw new IllegalStateException(
                        "It is not allowed that an instance of " + IDedicatedConverter.class.getName() + " returns a value of the same type (" + targetValue.getClass().getName() +
                                ") after conversion like " + dedicatedConverter + " did");
            }
            sourceValue = targetValue;
        }
        if (expectedType.isArray() && sourceValue != null) {
            var expectedComponentType = expectedType.getComponentType();
            if (sourceValue.getClass().isArray()) {
                // try to convert item by item of the array
                int size = Array.getLength(sourceValue);
                var targetValue = Array.newInstance(expectedComponentType, size);
                for (int a = size; a-- > 0; ) {
                    var sourceItem = Array.get(sourceValue, a);
                    var targetItem = convertValueToType(expectedComponentType, sourceItem, additionalInformation);
                    Array.set(targetValue, a, targetItem);
                }
                return (T) targetValue;
            } else {
                // try to create an array of length=1
                var array = Array.newInstance(expectedComponentType, 1);
                var targetItem = convertValueToType(expectedComponentType, sourceValue, additionalInformation);
                Array.set(array, 0, targetItem);
                return (T) array;
            }
        }
        return defaultConversionHelper.convertValueToType(expectedType, sourceValue, additionalInformation);
    }

    @Override
    public void registerDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType) {
        converters.register(dedicatedConverter, sourceType, targetType);
    }

    @Override
    public void unregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType) {
        converters.unregister(dedicatedConverter, sourceType, targetType);
    }

    @Override
    public <T> IPreparedConverter<T> prepareConverter(Class<T> expectedType, Object exemplaryValue) {
        return prepareConverter(expectedType, exemplaryValue, null);
    }

    @Override
    public <T> IPreparedConverter<T> prepareConverter(Class<T> expectedType, Object exemplaryValue, Object exemplaryAdditionalInformation) {
        if (exemplaryValue == null || expectedType == null) {
            return (value, additionalInformation) -> (T) value;
        }
        var sourceClass = exemplaryValue.getClass();
        if (expectedType.isAssignableFrom(sourceClass)) {
            return (value, additionalInformation) -> (T) value;
        }
        if (expectedType.isPrimitive() && exemplaryValue instanceof Number) {
            return defaultConversionHelper.prepareConverter(expectedType, exemplaryValue, exemplaryAdditionalInformation);
        }
        var dedicatedConverter = converters.getExtension(sourceClass, expectedType);
        if (dedicatedConverter != null) {
            Object targetValue;
            try {
                targetValue = dedicatedConverter.convertValueToType(expectedType, sourceClass, exemplaryValue, exemplaryAdditionalInformation);
            } catch (Throwable e) {
                throw RuntimeExceptionUtil.mask(e, "Error occured while converting value: " + exemplaryValue);
            }
            if (targetValue == null) {
                if (expectedType.isPrimitive()) {
                    throw new IllegalStateException(
                            "It is not allowed that an instance of " + IDedicatedConverter.class.getName() + " returns null like " + dedicatedConverter + " did for conversion from '" +
                                    sourceClass.getName() + "' to '" + expectedType + "'");
                }
                return (value, additionalInformation) -> null;
            }
            if (expectedType.isAssignableFrom(targetValue.getClass())) {
                return (value, additionalInformation) -> (T) dedicatedConverter.convertValueToType(expectedType, value.getClass(), value, additionalInformation);
            }
            if (targetValue.getClass().equals(exemplaryValue.getClass())) {
                throw new IllegalStateException(
                        "It is not allowed that an instance of " + IDedicatedConverter.class.getName() + " returns a value of the same type (" + targetValue.getClass().getName() +
                                ") after conversion like " + dedicatedConverter + " did");
            }
            var nextDedicatedConverter = converters.getExtension(targetValue.getClass(), expectedType);
            if (nextDedicatedConverter != null) {
                return (value, additionalInformation) -> convertDefault(expectedType, value, additionalInformation);
            }
        }
        if (!expectedType.isArray() || exemplaryValue == null) {
            var defaultConversionHelper = this.defaultConversionHelper;
            return (source, additionalInformation) -> defaultConversionHelper.convertValueToType(expectedType, source, additionalInformation);
        }
        var expectedComponentType = expectedType.getComponentType();
        if (exemplaryValue.getClass().isArray()) {
            // try to convert item by item of the array
            if (dedicatedConverter == null) {
                return (source, additionalInformation) -> {
                    var size = Array.getLength(source);
                    var targetValue = Array.newInstance(expectedComponentType, size);
                    for (int a = size; a-- > 0; ) {
                        var sourceItem = Array.get(source, a);
                        var targetItem = convertValueToType(expectedComponentType, sourceItem, exemplaryAdditionalInformation);
                        Array.set(targetValue, a, targetItem);
                    }
                    return (T) targetValue;
                };
            }
            return (source, additionalInformation) -> {
                source = dedicatedConverter.convertValueToType(expectedType, source.getClass(), source, additionalInformation);
                var size = Array.getLength(source);
                var targetValue = Array.newInstance(expectedComponentType, size);
                for (int a = size; a-- > 0; ) {
                    var sourceItem = Array.get(source, a);
                    var targetItem = convertValueToType(expectedComponentType, sourceItem, exemplaryAdditionalInformation);
                    Array.set(targetValue, a, targetItem);
                }
                return (T) targetValue;
            };
        }
        // try to create an array of length=1
        if (dedicatedConverter == null) {
            return (source, additionalInformation) -> {
                var array = Array.newInstance(expectedComponentType, 1);
                var targetItem = convertValueToType(expectedComponentType, source, additionalInformation);
                Array.set(array, 0, targetItem);
                return (T) array;
            };
        }
        return (source, additionalInformation) -> {
            var sourceValue = dedicatedConverter.convertValueToType(expectedType, source.getClass(), source, additionalInformation);
            var array = Array.newInstance(expectedComponentType, 1);
            var targetItem = convertValueToType(expectedComponentType, sourceValue, additionalInformation);
            Array.set(array, 0, targetItem);
            return (T) array;
        };
    }
}
