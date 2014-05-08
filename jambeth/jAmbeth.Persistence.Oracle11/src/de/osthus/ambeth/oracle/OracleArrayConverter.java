package de.osthus.ambeth.oracle;

import java.util.Collection;
import java.util.Set;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;
import de.osthus.ambeth.util.ParamChecker;

public class OracleArrayConverter implements IDedicatedConverter, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(conversionHelper, "ConversionHelper");
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (String.class.isAssignableFrom(expectedType))
		{
			Object tempValue = conversionHelper.convertValueToType(Collection.class, value);
			Object targetString = conversionHelper.convertValueToType(String.class, tempValue);
			return targetString;
		}
		if (!Collection.class.isAssignableFrom(expectedType))
		{
			// Not supported
			return null;
		}
		try
		{
			oracle.sql.ARRAY orray = (oracle.sql.ARRAY) value;
			// String sqlTypeName = orray.getSQLTypeName();
			// if (sqlTypeName.contains("."))
			// {
			// int pos = sqlTypeName.lastIndexOf(".");
			// sqlTypeName = sqlTypeName.substring(pos + 1);
			// }
			Class<?> componentType = (Class<?>) additionalInformation;

			Collection<Object> targetCollection = null;

			Object[] array = (Object[]) orray.getArray();

			if (Set.class.isAssignableFrom(expectedType))
			{
				targetCollection = new java.util.HashSet<Object>((int) ((array.length + 1) / 0.75f), 0.75f);
			}
			else
			{
				targetCollection = new java.util.ArrayList<Object>();
			}

			for (int i = 0, size = array.length; i < size; i++)
			{
				Object element = conversionHelper.convertValueToType(componentType, array[i]);
				targetCollection.add(element);
			}
			return targetCollection;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
