package de.osthus.ambeth.persistence.jdbc.array;

import java.sql.Array;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;
import de.osthus.ambeth.util.ParamChecker;

public class ArrayConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionExtension connectionExtension;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		try
		{
			if (Array.class.isAssignableFrom(sourceType))
			{
				Array array = (Array) value;

				ArrayList<Object> list = new ArrayList<Object>();
				ResultSet rs = array.getResultSet();
				try
				{
					Class<?> componentType = null;
					if (expectedType.isArray())
					{
						componentType = expectedType.getComponentType();
					}
					else if (Collection.class.isAssignableFrom(expectedType) && additionalInformation != null)
					{
						componentType = (Class<?>) additionalInformation;
					}
					while (rs.next())
					{
						int index = ((Number) rs.getObject(1)).intValue();
						Object item = rs.getObject(2);
						while (list.size() < index)
						{
							list.add(null);
						}
						item = conversionHelper.convertValueToType(componentType, item);
						list.set(index - 1, item);
					}
					if (expectedType.isArray())
					{
						Object targetArray = java.lang.reflect.Array.newInstance(componentType, list.size());
						for (int a = 0, size = list.size(); a < size; a++)
						{
							java.lang.reflect.Array.set(targetArray, a, list.get(a));
						}
						return targetArray;
					}
					else if (Set.class.isAssignableFrom(expectedType))
					{
						Set<Object> result = new java.util.HashSet<Object>((int) ((list.size() + 1) / 0.75f), 0.75f);
						result.addAll(list);
						return result;
					}
					else if (Collection.class.isAssignableFrom(expectedType))
					{
						java.util.ArrayList<Object> result = new java.util.ArrayList<Object>(list.size());
						result.addAll(list);
						return result;
					}
				}
				finally
				{
					JdbcUtil.close(rs);
				}
			}
			else if (sourceType.isArray())
			{
				if (Array.class.isAssignableFrom(expectedType))
				{
					ParamChecker.assertParamNotNull(additionalInformation, "additionalInformation");
					Class<?> componentType = (Class<?>) additionalInformation;
					return connectionExtension.createJDBCArray(componentType, value);
				}
				else if (Set.class.isAssignableFrom(expectedType))
				{
					Set<?> result = new java.util.HashSet<Object>(Arrays.asList(value));
					return result;
				}
			}
			else if (Collection.class.isAssignableFrom(sourceType))
			{
				if (Array.class.isAssignableFrom(expectedType))
				{
					ParamChecker.assertParamNotNull(additionalInformation, "additionalInformation");

					Object[] valueArray = ((Collection<?>) value).toArray();
					Class<?> componentType = (Class<?>) additionalInformation;
					return connectionExtension.createJDBCArray(componentType, valueArray);
				}
			}
			throw new IllegalArgumentException("Cannot convert from '" + sourceType + "' to '" + expectedType
					+ "'. This is a bug if I get called for types which I do not support and I did not register with!");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
