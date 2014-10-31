package de.osthus.ambeth.util;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class DelegatingConversionHelper extends ClassTupleExtendableContainer<IDedicatedConverter> implements IInitializingBean, IDedicatedConverterExtendable,
		IConversionHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IConversionHelper defaultConversionHelper;

	public DelegatingConversionHelper()
	{
		super("dedicatedConverter", "type", true);
	}

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
		try
		{
			Object targetValue = value;
			while (true)
			{
				Class<?> targetClass = targetValue.getClass();
				IDedicatedConverter dedicatedConverter = getExtension(targetClass, expectedType);
				if (dedicatedConverter == null)
				{
					break;
				}
				Object newTargetValue = dedicatedConverter.convertValueToType(expectedType, targetClass, targetValue, additionalInformation);
				if (newTargetValue == null)
				{
					if (expectedType.isPrimitive())
					{
						throw new IllegalStateException("It is not allowed that an instance of " + IDedicatedConverter.class.getName() + " returns null like "
								+ dedicatedConverter + " did for conversion from '" + targetClass.getName() + "' to '" + expectedType + "'");
					}
					return null;
				}
				if (expectedType.isAssignableFrom(newTargetValue.getClass()))
				{
					return (T) newTargetValue;
				}
				if (newTargetValue.getClass().equals(targetValue.getClass()))
				{
					throw new IllegalStateException("It is not allowed that an instance of " + IDedicatedConverter.class.getName()
							+ " returns a value of the same type (" + newTargetValue.getClass().getName() + ") after conversion like " + dedicatedConverter
							+ " did");
				}
				targetValue = newTargetValue;
			}
			return defaultConversionHelper.convertValueToType(expectedType, targetValue, additionalInformation);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void registerDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType)
	{
		register(dedicatedConverter, sourceType, targetType);
	}

	@Override
	public void unregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Class<?> sourceType, Class<?> targetType)
	{
		unregister(dedicatedConverter, sourceType, targetType);
	}
}
