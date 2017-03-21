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

import java.lang.reflect.Array;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DelegatingConversionHelper extends IConversionHelper implements IInitializingBean, IDedicatedConverterExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ClassTupleExtendableContainer<IDedicatedConverter> converters = new ClassTupleExtendableContainer<IDedicatedConverter>(
			"dedicatedConverter", "type", true);

	protected IConversionHelper defaultConversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(defaultConversionHelper, "defaultConversionHelper");
	}

	public void setDefaultConversionHelper(IConversionHelper defaultConversionHelper)
	{
		this.defaultConversionHelper = defaultConversionHelper;
	}

	@Override
	public <T> T convertValueToType(Class<T> expectedType, Object value)
	{
		return convertValueToType(expectedType, value, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertValueToType(Class<T> expectedType, Object value, Object additionalInformation)
	{
		if (value == null)
		{
			return null;
		}
		if (expectedType == null || expectedType.isAssignableFrom(value.getClass()))
		{
			return (T) value;
		}
		if (expectedType.isPrimitive() && value instanceof Number)
		{
			return defaultConversionHelper.convertValueToType(expectedType, value);
		}
		Object sourceValue = value;
		while (true)
		{
			Class<?> sourceClass = sourceValue.getClass();
			IDedicatedConverter dedicatedConverter = converters.getExtension(sourceClass, expectedType);
			if (dedicatedConverter == null)
			{
				break;
			}
			Object targetValue;
			try
			{
				targetValue = dedicatedConverter.convertValueToType(expectedType, sourceClass, sourceValue, additionalInformation);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e, "Error occured while converting value: " + sourceValue);
			}
			if (targetValue == null)
			{
				if (expectedType.isPrimitive())
				{
					throw new IllegalStateException("It is not allowed that an instance of " + IDedicatedConverter.class.getName() + " returns null like "
							+ dedicatedConverter + " did for conversion from '" + sourceClass.getName() + "' to '" + expectedType + "'");
				}
				return null;
			}
			if (expectedType.isAssignableFrom(targetValue.getClass()))
			{
				return (T) targetValue;
			}
			if (targetValue.getClass().equals(sourceValue.getClass()))
			{
				throw new IllegalStateException("It is not allowed that an instance of " + IDedicatedConverter.class.getName()
						+ " returns a value of the same type (" + targetValue.getClass().getName() + ") after conversion like " + dedicatedConverter + " did");
			}
			sourceValue = targetValue;
		}
		if (expectedType.isArray() && sourceValue != null)
		{
			Class<?> expectedComponentType = expectedType.getComponentType();
			if (sourceValue.getClass().isArray())
			{
				// try to convert item by item of the array
				int size = Array.getLength(sourceValue);
				Object targetValue = Array.newInstance(expectedComponentType, size);
				for (int a = size; a-- > 0;)
				{
					Object sourceItem = Array.get(sourceValue, a);
					Object targetItem = convertValueToType(expectedComponentType, sourceItem, additionalInformation);
					Array.set(targetValue, a, targetItem);
				}
				return (T) targetValue;
			}
			else
			{
				// try to create an array of length=1
				Object array = Array.newInstance(expectedComponentType, 1);
				Object targetItem = convertValueToType(expectedComponentType, sourceValue, additionalInformation);
				Array.set(array, 0, targetItem);
				return (T) array;
			}
		}
		return defaultConversionHelper.convertValueToType(expectedType, sourceValue, additionalInformation);
	}

	@Override
	public void registerDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType)
	{
		converters.register(dedicatedConverter, sourceType, targetType);
	}

	@Override
	public void unregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType)
	{
		converters.unregister(dedicatedConverter, sourceType, targetType);
	}
}
