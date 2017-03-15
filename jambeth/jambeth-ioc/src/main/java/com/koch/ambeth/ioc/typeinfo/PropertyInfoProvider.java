package com.koch.ambeth.ioc.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.annotation.PropertyAccessor;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class PropertyInfoProvider implements IPropertyInfoProvider, IInitializingBean {
	private static final Pattern getSetIsPattern = Pattern.compile("(get|set|is)([A-ZÖÄÜ].*)");

	protected IThreadLocalObjectCollector objectCollector;

	protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToPropertyMap =
			new SmartCopyMap<>();

	protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToIocPropertyMap =
			new SmartCopyMap<>();

	protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToPrivatePropertyMap =
			new SmartCopyMap<>();

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
		this.objectCollector = objectCollector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo getProperty(Object obj, String propertyName) {
		return getProperty(obj.getClass(), propertyName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo getProperty(Class<?> type, String propertyName) {
		Map<String, IPropertyInfo> map = getPropertyMap(type);
		return map.get(propertyName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo[] getProperties(Object obj) {
		return getProperties(obj.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPropertyInfo[] getProperties(Class<?> type) {
		return getPropertyEntry(type, typeToPropertyMap, true, false).properties;
	}

	@Override
	public IPropertyInfo[] getIocProperties(Class<?> type) {
		return getPropertyEntry(type, typeToIocPropertyMap, true, true).properties;
	}

	@Override
	public IPropertyInfo[] getPrivateProperties(Class<?> type) {
		return getPropertyEntry(type, typeToPrivatePropertyMap, false, false).properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMap<String, IPropertyInfo> getPropertyMap(Object obj) {
		return getPropertyMap(obj.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMap<String, IPropertyInfo> getPropertyMap(Class<?> type) {
		return getPropertyEntry(type, typeToPropertyMap, true, false).map;
	}

	@Override
	public IMap<String, IPropertyInfo> getIocPropertyMap(Class<?> type) {
		return getPropertyEntry(type, typeToIocPropertyMap, true, true).map;
	}

	@Override
	public IMap<String, IPropertyInfo> getPrivatePropertyMap(Class<?> type) {
		return getPropertyEntry(type, typeToPrivatePropertyMap, false, false).map;
	}

	protected PropertyInfoEntry getPropertyEntry(Class<?> type,
			SmartCopyMap<Class<?>, PropertyInfoEntry> map, boolean isOldIocMode, boolean isIocMode) {
		ParamChecker.assertParamNotNull(type, "type");
		PropertyInfoEntry propertyEntry = map.get(type);
		if (propertyEntry != null) {
			return propertyEntry;
		}
		Lock writeLock = map.getWriteLock();
		writeLock.lock();
		try {
			propertyEntry = map.get(type);
			if (propertyEntry != null) {
				// Concurrent thread might have been faster
				return propertyEntry;
			}

			IMap<String, IMap<Class<?>, IMap<String, Method>>> sortedMethods =
					new LinkedHashMap<>();
			Method[] methods = ReflectUtil.getDeclaredMethodsInHierarchy(type);

			MethodAccess methodAccess = null;
			for (int i = methods.length; i-- > 0;) {
				Method method = methods[i];
				if (method.getDeclaringClass().equals(Object.class)) {
					continue;
				}
				int modifiers = method.getModifiers();
				if (Modifier.isStatic(modifiers)) {
					continue;
				}
				try {
					String propName = getPropertyNameFor(method);
					if (propName.isEmpty()) {
						continue;
					}
					IMap<Class<?>, IMap<String, Method>> sortedMethod = sortedMethods.get(propName);
					if (sortedMethod == null) {
						sortedMethod = LinkedHashMap.create(1);
						sortedMethods.put(propName, sortedMethod);
					}

					Class<?>[] parameterTypes = method.getParameterTypes();
					Class<?> propertyType;
					String prefix;
					if (parameterTypes.length == 1) {
						propertyType = parameterTypes[0];
						prefix = "set";
					}
					else if (parameterTypes.length == 0) {
						propertyType = method.getReturnType();
						prefix = "get";
					}
					else {
						throw new IllegalStateException("Method is not an accessor: " + method);
					}

					IMap<String, Method> methodPerType = sortedMethod.get(propertyType);
					if (methodPerType == null) {
						methodPerType = LinkedHashMap.create(2);
						sortedMethod.put(propertyType, methodPerType);
					}

					methodPerType.put(prefix, method);
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e, "Error occured while processing " + method);
				}
			}

			IMap<String, IMap<String, Method>> filteredMethods =
					filterOverriddenMethods(sortedMethods, type);

			LinkedHashMap<String, IPropertyInfo> propertyMap =
					new LinkedHashMap<>(0.5f);
			for (Entry<String, IMap<String, Method>> propertyData : filteredMethods) {
				String propertyName = propertyData.getKey();
				IMap<String, Method> propertyMethods = propertyData.getValue();
				Method getter = propertyMethods.get("get");
				Method setter = propertyMethods.get("set");

				if (isIocMode) {
					if (setter == null || (!Modifier.isPublic(setter.getModifiers())
							&& !setter.isAnnotationPresent(Autowired.class)
							&& !setter.isAnnotationPresent(Property.class))) {
						continue;
					}
				}
				IPropertyInfo propertyInfo;
				if (methodAccess == null && !type.isInterface() && !type.isPrimitive()) {
					methodAccess = MethodAccess.get(type);
				}
				if (methodAccess != null && isNullOrNonAbstractNonPrivateMethod(getter)
						&& isNullOrNonAbstractNonPrivateMethod(setter)) {
					propertyInfo = new MethodPropertyInfoASM(type, propertyName, getter, setter,
							objectCollector, methodAccess);
				}
				else {
					propertyInfo =
							new MethodPropertyInfo(type, propertyName, getter, setter, objectCollector);
				}
				propertyMap.put(propertyInfo.getName(), propertyInfo);
			}

			FieldAccess fieldAccess = null;
			Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(type);
			for (Field field : fields) {
				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers)) {
					continue;
				}
				if (isOldIocMode) {
					if (!field.isAnnotationPresent(Autowired.class)
							&& !field.isAnnotationPresent(Property.class)) {
						continue;
					}
				}
				String propertyName = getPropertyNameFor(field);
				IPropertyInfo existingProperty = propertyMap.get(propertyName);
				if (existingProperty != null && existingProperty.isWritable()) {
					// Ignore field injection if the already resolved (method-)property is writable
					continue;
				}
				IPropertyInfo propertyInfo;
				if (fieldAccess == null && !type.isInterface() && !type.isPrimitive()) {
					fieldAccess = FieldAccess.get(type);
				}
				if (fieldAccess != null && (field.getModifiers() & Modifier.PRIVATE) == 0) {
					FieldAccess fieldAccessOfDeclaringType = field.getDeclaringClass() != type
							? FieldAccess.get(field.getDeclaringClass()) : fieldAccess;// type.getClassLoader().loadClass("com.koch.ambeth.event.ioc.EventModule")
					propertyInfo = new FieldPropertyInfoASM(type, propertyName, field, objectCollector,
							fieldAccessOfDeclaringType);
				}
				else {
					propertyInfo = new FieldPropertyInfo(type, propertyName, field, objectCollector);
				}
				propertyMap.put(propertyInfo.getName(), propertyInfo);
			}
			propertyEntry = new PropertyInfoEntry(propertyMap);
			map.put(type, propertyEntry);
			return propertyEntry;
		}
		finally {
			writeLock.unlock();
		}
	}

	protected boolean isNullOrNonAbstractNonPrivateMethod(Method method) {
		if (method == null) {
			return true;
		}
		return !Modifier.isAbstract(method.getModifiers())
				&& !Modifier.isPrivate(method.getModifiers());
	}

	protected IMap<String, IMap<String, Method>> filterOverriddenMethods(
			IMap<String, IMap<Class<?>, IMap<String, Method>>> sortedMethods, Class<?> entityType) {
		IMap<String, IMap<String, Method>> filteredMethods = LinkedHashMap.create(sortedMethods.size());

		for (Entry<String, IMap<Class<?>, IMap<String, Method>>> entry : sortedMethods) {
			String propName = entry.getKey();
			IMap<Class<?>, IMap<String, Method>> typedHashMap = entry.getValue();

			if (typedHashMap.size() == 1) {
				Iterator<IMap<String, Method>> iter = typedHashMap.values().iterator();
				IMap<String, Method> accessorMap = iter.next();
				filteredMethods.put(propName, accessorMap);
				continue;
			}

			Class<?> mostConcreteType = null;
			Class<?> mostConcreteGetterType = null;
			Class<?> mostConcreteSetterType = null;
			for (Entry<Class<?>, IMap<String, Method>> typedEntries : typedHashMap) {
				Class<?> currentType = typedEntries.getKey();
				IMap<String, Method> accessorMap = typedEntries.getValue();
				if (accessorMap.size() != 2) {
					if (accessorMap.get("get") != null) {
						mostConcreteGetterType = resolveMostConcreteType(mostConcreteGetterType, currentType);
					}
					else {
						mostConcreteSetterType = resolveMostConcreteType(mostConcreteSetterType, currentType);
					}
					continue;
				}
				mostConcreteType = resolveMostConcreteType(mostConcreteType, currentType);
			}
			if (mostConcreteType != null) {
				IMap<String, Method> accessorMap = typedHashMap.get(mostConcreteType);
				filteredMethods.put(propName, accessorMap);
			}
			else if (mostConcreteGetterType != null) {
				IMap<String, Method> accessorMap = typedHashMap.get(mostConcreteGetterType);
				filteredMethods.put(propName, accessorMap);
			}
			else if (mostConcreteSetterType != null) {
				IMap<String, Method> accessorMap = typedHashMap.get(mostConcreteSetterType);
				filteredMethods.put(propName, accessorMap);
			}
		}

		return filteredMethods;
	}

	protected Class<?> resolveMostConcreteType(Class<?> mostConcreteType, Class<?> currentType) {
		if (mostConcreteType == null || mostConcreteType.isAssignableFrom(currentType)) {
			return currentType;
		}
		return mostConcreteType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPropertyNameFor(Method method) {
		PropertyAccessor propertyAccessor = method.getAnnotation(PropertyAccessor.class);
		if (propertyAccessor != null) {
			return propertyAccessor.value();
		}
		Matcher matcher = getSetIsPattern.matcher(method.getName());
		if (!matcher.matches()) {
			return "";
		}
		int paramLength = method.getParameterTypes().length;
		String getSetIs = matcher.group(1);
		if ("get".equals(getSetIs) && (0 != paramLength || void.class.equals(method.getReturnType()))) {
			return "";
		}
		else if ("set".equals(getSetIs) && 1 != paramLength) {
			return "";
		}
		else if ("is".equals(getSetIs)
				&& (0 != paramLength || void.class.equals(method.getReturnType()))) {
			return "";
		}
		String name = matcher.group(2);
		return StringConversionHelper.upperCaseFirst(objectCollector, name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPropertyNameFor(Field field) {
		return StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
	}
}
