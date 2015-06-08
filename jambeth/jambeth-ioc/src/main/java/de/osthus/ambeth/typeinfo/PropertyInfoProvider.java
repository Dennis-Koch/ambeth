package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

public class PropertyInfoProvider implements IPropertyInfoProvider, IInitializingBean
{
	private static final Pattern getSetIsPattern = Pattern.compile("(get|set|is)([A-ZÖÄÜ].*)");

	protected IThreadLocalObjectCollector objectCollector;

	protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToIocPropertyMap = new SmartCopyMap<Class<?>, PropertyInfoEntry>();

	protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToPrivatePropertyMap = new SmartCopyMap<Class<?>, PropertyInfoEntry>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo getProperty(Object obj, String propertyName)
	{
		return getProperty(obj.getClass(), propertyName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo getProperty(Class<?> type, String propertyName)
	{
		Map<String, IPropertyInfo> map = getPropertyMap(type);
		return map.get(propertyName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo[] getProperties(Object obj)
	{
		return getProperties(obj.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo[] getProperties(Class<?> type)
	{
		return getPropertyEntry(type, typeToIocPropertyMap, true, false).properties;
	}

	@Override
	public IPropertyInfo[] getIocProperties(Class<?> type)
	{
		return getPropertyEntry(type, typeToIocPropertyMap, true, true).properties;
	}

	@Override
	public IPropertyInfo[] getPrivateProperties(Class<?> type)
	{
		return getPropertyEntry(type, typeToPrivatePropertyMap, false, false).properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMap<String, IPropertyInfo> getPropertyMap(Object obj)
	{
		return getPropertyMap(obj.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMap<String, IPropertyInfo> getPropertyMap(Class<?> type)
	{
		return getPropertyEntry(type, typeToIocPropertyMap, true, false).map;
	}

	@Override
	public IMap<String, IPropertyInfo> getIocPropertyMap(Class<?> type)
	{
		return getPropertyEntry(type, typeToIocPropertyMap, true, true).map;
	}

	@Override
	public IMap<String, IPropertyInfo> getPrivatePropertyMap(Class<?> type)
	{
		return getPropertyEntry(type, typeToPrivatePropertyMap, false, false).map;
	}

	protected PropertyInfoEntry getPropertyEntry(Class<?> type, SmartCopyMap<Class<?>, PropertyInfoEntry> map, boolean isOldIocMode, boolean isIocMode)
	{
		ParamChecker.assertParamNotNull(type, "type");
		PropertyInfoEntry propertyEntry = map.get(type);
		if (propertyEntry != null)
		{
			return propertyEntry;
		}
		Lock writeLock = map.getWriteLock();
		writeLock.lock();
		try
		{
			propertyEntry = map.get(type);
			if (propertyEntry != null)
			{
				// Concurrent thread might have been faster
				return propertyEntry;
			}

			HashMap<String, HashMap<Class<?>, HashMap<String, Method>>> sortedMethods = new HashMap<String, HashMap<Class<?>, HashMap<String, Method>>>();
			Method[] methods = ReflectUtil.getDeclaredMethodsInHierarchy(type);

			MethodAccess methodAccess = null;
			for (int i = methods.length; i-- > 0;)
			{
				Method method = methods[i];
				if (method.getDeclaringClass().equals(Object.class))
				{
					continue;
				}
				int modifiers = method.getModifiers();
				if (Modifier.isStatic(modifiers))
				{
					continue;
				}
				try
				{
					String propName = getPropertyNameFor(method);
					if (propName.isEmpty())
					{
						continue;
					}
					HashMap<Class<?>, HashMap<String, Method>> sortedMethod = sortedMethods.get(propName);
					if (sortedMethod == null)
					{
						sortedMethod = HashMap.create(1);
						sortedMethods.put(propName, sortedMethod);
					}

					Class<?>[] parameterTypes = method.getParameterTypes();
					Class<?> propertyType;
					String prefix;
					if (parameterTypes.length == 1)
					{
						propertyType = parameterTypes[0];
						prefix = "set";
					}
					else if (parameterTypes.length == 0)
					{
						propertyType = method.getReturnType();
						prefix = "get";
					}
					else
					{
						throw new IllegalStateException("Method is not an accessor: " + method);
					}

					HashMap<String, Method> methodPerType = sortedMethod.get(propertyType);
					if (methodPerType == null)
					{
						methodPerType = HashMap.create(2);
						sortedMethod.put(propertyType, methodPerType);
					}

					methodPerType.put(prefix, method);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e, "Error occured while processing " + method);
				}
			}

			HashMap<String, HashMap<String, Method>> filteredMethods = filterOverriddenMethods(sortedMethods, type);

			HashMap<String, IPropertyInfo> propertyMap = new HashMap<String, IPropertyInfo>(0.5f);
			for (Entry<String, HashMap<String, Method>> propertyData : filteredMethods)
			{
				String propertyName = propertyData.getKey();
				HashMap<String, Method> propertyMethods = propertyData.getValue();
				Method getter = propertyMethods.get("get");
				Method setter = propertyMethods.get("set");

				if (isIocMode)
				{
					if (setter == null
							|| (!Modifier.isPublic(setter.getModifiers()) && !setter.isAnnotationPresent(Autowired.class) && !setter
									.isAnnotationPresent(Property.class)))
					{
						continue;
					}
				}
				IPropertyInfo propertyInfo;
				if (methodAccess == null && !type.isInterface() && !type.isPrimitive())
				{
					methodAccess = MethodAccess.get(type);
				}
				if (methodAccess != null && isNullOrNonAbstractNonPrivateMethod(getter) && isNullOrNonAbstractNonPrivateMethod(setter))
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
				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers))
				{
					continue;
				}
				if (isOldIocMode)
				{
					if (!field.isAnnotationPresent(Autowired.class) && !field.isAnnotationPresent(Property.class))
					{
						continue;
					}
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
			map.put(type, propertyEntry);
			return propertyEntry;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected boolean isNullOrNonAbstractNonPrivateMethod(Method method)
	{
		if (method == null)
		{
			return true;
		}
		return !Modifier.isAbstract(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers());
	}

	protected HashMap<String, HashMap<String, Method>> filterOverriddenMethods(HashMap<String, HashMap<Class<?>, HashMap<String, Method>>> sortedMethods,
			Class<?> entityType)
	{
		HashMap<String, HashMap<String, Method>> filteredMethods = HashMap.create(sortedMethods.size());

		for (Entry<String, HashMap<Class<?>, HashMap<String, Method>>> entry : sortedMethods)
		{
			String propName = entry.getKey();
			HashMap<Class<?>, HashMap<String, Method>> typedHashMap = entry.getValue();

			if (typedHashMap.size() == 1)
			{
				Iterator<HashMap<String, Method>> iter = typedHashMap.values().iterator();
				HashMap<String, Method> accessorMap = iter.next();
				filteredMethods.put(propName, accessorMap);
				continue;
			}

			Class<?> mostConcreteType = null;
			Class<?> mostConcreteGetterType = null;
			Class<?> mostConcreteSetterType = null;
			for (Entry<Class<?>, HashMap<String, Method>> typedEntries : typedHashMap)
			{
				Class<?> currentType = typedEntries.getKey();
				HashMap<String, Method> accessorMap = typedEntries.getValue();
				if (accessorMap.size() != 2)
				{
					if (accessorMap.get("get") != null)
					{
						if (mostConcreteGetterType == null || mostConcreteGetterType.isAssignableFrom(currentType))
						{
							mostConcreteGetterType = currentType;
						}
					}
					else
					{
						if (mostConcreteSetterType == null || mostConcreteSetterType.isAssignableFrom(currentType))
						{
							mostConcreteSetterType = currentType;
						}
					}
					continue;
				}

				if (mostConcreteType == null || mostConcreteType.isAssignableFrom(currentType))
				{
					mostConcreteType = currentType;
				}
			}
			if (mostConcreteType != null)
			{
				HashMap<String, Method> accessorMap = typedHashMap.get(mostConcreteType);
				filteredMethods.put(propName, accessorMap);
			}
			else if (mostConcreteGetterType != null)
			{
				HashMap<String, Method> accessorMap = typedHashMap.get(mostConcreteGetterType);
				filteredMethods.put(propName, accessorMap);
			}
			else if (mostConcreteSetterType != null)
			{
				HashMap<String, Method> accessorMap = typedHashMap.get(mostConcreteSetterType);
				filteredMethods.put(propName, accessorMap);
			}
		}

		return filteredMethods;
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@Override
	public String getPropertyNameFor(Field field)
	{
		return StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
	}
}
