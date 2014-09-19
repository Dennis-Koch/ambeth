package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.annotation.PropertyAccessor;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.StringConversionHelper;

public class PropertyInfoProvider extends SmartCopyMap<Class<?>, PropertyInfoEntry> implements IPropertyInfoProvider, IInitializingBean
{
	private static final Pattern getSetIsPattern = Pattern.compile("(get|set|is)([A-ZÖÄÜ].*)");

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.typeinfo.IPropertyInfoProvider#getProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public IPropertyInfo getProperty(Object obj, String propertyName)
	{
		return getProperty(obj.getClass(), propertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.typeinfo.IPropertyInfoProvider#getProperty(java.lang.Class, java.lang.String)
	 */
	@Override
	public IPropertyInfo getProperty(Class<?> type, String propertyName)
	{
		Map<String, IPropertyInfo> map = getPropertyMap(type);
		return map.get(propertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.typeinfo.IPropertyInfoProvider#getProperties(java.lang.Object)
	 */
	@Override
	public IPropertyInfo[] getProperties(Object obj)
	{
		return getProperties(obj.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.typeinfo.IPropertyInfoProvider#getProperties(java.lang.Class)
	 */
	@Override
	public IPropertyInfo[] getProperties(Class<?> type)
	{
		return getPropertyEntry(type).properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.typeinfo.IPropertyInfoProvider#getPropertyMap(java.lang.Object)
	 */
	@Override
	public IMap<String, IPropertyInfo> getPropertyMap(Object obj)
	{
		return getPropertyMap(obj.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.typeinfo.IPropertyInfoProvider#getPropertyMap(java.lang.Class)
	 */
	@Override
	public IMap<String, IPropertyInfo> getPropertyMap(Class<?> type)
	{
		return getPropertyEntry(type).map;
	}

	protected PropertyInfoEntry getPropertyEntry(Class<?> type)
	{
		ParamChecker.assertParamNotNull(type, "type");
		PropertyInfoEntry propertyEntry = get(type);
		if (propertyEntry != null)
		{
			return propertyEntry;
		}
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			propertyEntry = get(type);
			if (propertyEntry != null)
			{
				// Concurrent thread might have been faster
				return propertyEntry;
			}
			HashMap<String, Map<String, Method>> sortedMethods = new HashMap<String, Map<String, Method>>();
			Method[] methods = ReflectUtil.getMethods(type);

			MethodAccess methodAccess = null;
			for (int i = methods.length; i-- > 0;)
			{
				Method method = methods[i];
				if (method.getDeclaringClass().equals(Object.class))
				{
					continue;
				}
				try
				{
					String propName = getPropertyNameFor(method);
					int modifiers = method.getModifiers();
					if (propName.isEmpty() || Modifier.isStatic(modifiers))
					{
						continue;
					}
					Map<String, Method> sortedMethod = sortedMethods.get(propName);
					if (sortedMethod == null)
					{
						sortedMethod = HashMap.create(2);
						sortedMethods.put(propName, sortedMethod);
					}
					if (method.getParameterTypes().length == 1)
					{
						sortedMethod.put("set", method);
					}
					else if (method.getParameterTypes().length == 0)
					{
						sortedMethod.put("get", method);
					}
					else
					{
						throw new IllegalStateException("Method is not an accessor: " + method);
					}
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e, "Error occured while processing " + method);
				}
			}
			HashMap<String, IPropertyInfo> propertyMap = new HashMap<String, IPropertyInfo>(0.5f);

			for (Entry<String, Map<String, Method>> propertyData : sortedMethods)
			{
				String propertyName = propertyData.getKey();
				Map<String, Method> propertyMethods = propertyData.getValue();
				Method getter = propertyMethods.get("get");
				Method setter = propertyMethods.get("set");

				IPropertyInfo propertyInfo;
				if (methodAccess == null && !type.isInterface() && !type.isPrimitive())
				{
					methodAccess = MethodAccess.get(type);
				}
				if (methodAccess != null
						&& (getter == null || !Modifier.isAbstract(getter.getModifiers()) && (setter == null || !Modifier.isAbstract(setter.getModifiers()))))
				{
					propertyInfo = new MethodPropertyInfoASM(type, propertyName, getter, setter, objectCollector, methodAccess);
				}
				else
				{
					propertyInfo = new MethodPropertyInfo(type, propertyName, getter, setter, objectCollector);
				}
				propertyMap.put(propertyInfo.getName(), propertyInfo);
			}

			FieldAccess fieldAccess = null;
			Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(type);
			for (Field field : fields)
			{
				if (!field.isAnnotationPresent(Autowired.class) && !field.isAnnotationPresent(Property.class))
				{
					continue;
				}
				String propertyName = getPropertyNameFor(field);
				IPropertyInfo existingProperty = propertyMap.get(propertyName);
				if (existingProperty != null && existingProperty.isWritable())
				{
					// Ignore field injection if the already resolved (method-)property is writable
					continue;
				}
				IPropertyInfo propertyInfo;
				if (fieldAccess == null && !type.isInterface() && !type.isPrimitive())
				{
					fieldAccess = FieldAccess.get(type);
				}
				if (fieldAccess != null && (field.getModifiers() & Modifier.PRIVATE) == 0)
				{
					FieldAccess fieldAccessOfDeclaringType = FieldAccess.get(field.getDeclaringClass());
					propertyInfo = new FieldPropertyInfoASM(type, propertyName, field, objectCollector, fieldAccessOfDeclaringType);
				}
				else
				{
					propertyInfo = new FieldPropertyInfo(type, propertyName, field, objectCollector);
				}
				propertyMap.put(propertyInfo.getName(), propertyInfo);
			}
			propertyEntry = new PropertyInfoEntry(propertyMap);
			put(type, propertyEntry);
			return propertyEntry;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	/**
	 * @inhericDoc
	 */
	@Override
	public String getPropertyNameFor(Method method)
	{
		PropertyAccessor propertyAccessor = method.getAnnotation(PropertyAccessor.class);
		if (propertyAccessor != null)
		{
			return propertyAccessor.value();
		}
		Matcher matcher = getSetIsPattern.matcher(method.getName());
		if (!matcher.matches())
		{
			return "";
		}
		int paramLength = method.getParameterTypes().length;
		String getSetIs = matcher.group(1);
		if ("get".equals(getSetIs) && (0 != paramLength || void.class.equals(method.getReturnType())))
		{
			return "";
		}
		else if ("set".equals(getSetIs) && 1 != paramLength)
		{
			return "";
		}
		else if ("is".equals(getSetIs) && (0 != paramLength || void.class.equals(method.getReturnType())))
		{
			return "";
		}
		String name = matcher.group(2);
		return StringConversionHelper.upperCaseFirst(objectCollector, name);
	}

	/**
	 * @inhericDoc
	 */
	@Override
	public String getPropertyNameFor(Field field)
	{
		return StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
	}
}
