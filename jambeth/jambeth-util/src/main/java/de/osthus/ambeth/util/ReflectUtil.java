package de.osthus.ambeth.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.WeakHashMap;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public final class ReflectUtil
{
	public static class ReflectEntry
	{
		Constructor<?>[] constructors;

		Method[] methods;

		Field[] declaredFields;

		Field[] declaredFieldsInHierarchy;

		HashMap<String, Field> nameToDeclaredFieldMap;

		HashMap<String, Field[]> nameToDeclaredFieldsInHierarchyMap;

		Method[] declaredMethods;

		Method[] declaredMethodsInHierarchy;
	}

	private static final Field[] EMPTY_FIELDS = new Field[0];

	private static final WeakHashMap<Class<?>, Reference<ReflectEntry>> typeToMethodsMap = new WeakHashMap<Class<?>, Reference<ReflectEntry>>();

	private static final java.util.concurrent.locks.Lock writeLock = new ReentrantLock();

	protected static final ReflectEntry getReflectEntry(Class<?> type)
	{
		Reference<ReflectEntry> entryR = typeToMethodsMap.get(type);
		if (entryR != null)
		{
			ReflectEntry entry = entryR.get();
			if (entry != null)
			{
				return entry;
			}
		}
		ReflectEntry entry = new ReflectEntry();
		typeToMethodsMap.put(type, new WeakReference<ReflectEntry>(entry));
		return entry;
	}

	public static final Constructor<?>[] getConstructors(Class<?> type)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			Constructor<?>[] constructors = entry.constructors;
			if (constructors != null)
			{
				return constructors;
			}
			constructors = type.getConstructors();
			for (Constructor<?> constructor : constructors)
			{
				constructor.setAccessible(true);
			}
			entry.constructors = constructors;
			return constructors;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static final Method[] getMethods(Class<?> type)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			Method[] methods = entry.methods;
			if (methods != null)
			{
				return methods;
			}
			methods = type.getMethods();
			for (Method method : methods)
			{
				method.setAccessible(true);
			}
			entry.methods = methods;
			return methods;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static final Field[] getDeclaredFields(Class<?> type)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			Field[] declaredFields = entry.declaredFields;
			if (declaredFields != null)
			{
				return declaredFields;
			}
			initDeclaredFields(type, entry);
			return entry.declaredFields;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static final Field[] getDeclaredFieldsInHierarchy(Class<?> type)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			Field[] declaredFieldsInHierarchy = entry.declaredFieldsInHierarchy;
			if (declaredFieldsInHierarchy != null)
			{
				return declaredFieldsInHierarchy;
			}
			initDeclaredFields(type, entry);
			return entry.declaredFieldsInHierarchy;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static final Method[] getDeclaredMethods(Class<?> type)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			Method[] declaredMethods = entry.declaredMethods;
			if (declaredMethods != null)
			{
				return declaredMethods;
			}
			initDeclaredMethods(type, entry);
			return entry.declaredMethods;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static final Method[] getDeclaredMethodsInHierarchy(Class<?> type)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			Method[] declaredMethodsInHierarchy = entry.declaredMethodsInHierarchy;
			if (declaredMethodsInHierarchy != null)
			{
				return declaredMethodsInHierarchy;
			}
			initDeclaredMethods(type, entry);
			return entry.declaredMethodsInHierarchy;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected static void fillDeclaredMethods(Class<?> type, ArrayList<Method> declaredMethods)
	{
		declaredMethods.addAll(type.getDeclaredMethods());
	}

	// public static final Method getDeclaredMethod(boolean tryOnly, Class<?> type, String methodName)
	// {
	// Class<?> currType = type;
	// while (currType != null)
	// {
	// Method method = getDeclaredMethodIntern(currType, (Type) null, methodName, null, true);
	// if (method != null)
	// {
	// return method;
	// }
	// currType = currType.getSuperclass();
	// }
	// if (tryOnly)
	// {
	// return null;
	// }
	// throw new IllegalArgumentException(type + " does not implement '" + methodName + "'");
	// }

	public static final Method getDeclaredMethod(boolean tryOnly, Class<?> type, Class<?> returnType, String methodName, Class<?>... parameters)
	{
		Class<?> currType = type;
		Type[] params = parameters != null ? TypeUtil.getClassesToTypes(parameters) : null;
		Type returnTypeAsType = returnType != null ? Type.getType(returnType) : null;
		while (currType != null)
		{
			Method method = getDeclaredMethodIntern(currType, returnTypeAsType, methodName, params, true);
			if (method != null)
			{
				return method;
			}
			currType = currType.getSuperclass();
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException(type + " does not implement '" + methodName + "(" + Arrays.toString(parameters) + ")'");
	}

	private static final Method getDeclaredMethodIntern(Class<?> type, Type returnType, String methodName, Type[] parameters, boolean tryOnly)
	{
		Method[] declaredMethods = getDeclaredMethods(type);
		for (int a = declaredMethods.length; a-- > 0;)
		{
			Method declaredMethod = declaredMethods[a];
			if (!declaredMethod.getName().equals(methodName))
			{
				continue;
			}
			if (returnType != null && !Type.getType(declaredMethod.getReturnType()).equals(returnType))
			{
				continue;
			}
			if (parameters == null)
			{
				return declaredMethod;
			}
			Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
			if (parameterTypes.length != parameters.length)
			{
				continue;
			}
			boolean sameParams = true;
			for (int b = parameterTypes.length; b-- > 0;)
			{
				if (parameters[b] != null && !Type.getType(parameterTypes[b]).equals(parameters[b]))
				{
					sameParams = false;
					break;
				}
			}
			if (sameParams)
			{
				return declaredMethod;
			}
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException(type + " does not implement '" + methodName + "'");
	}

	public static final Method getDeclaredMethod(boolean tryOnly, Class<?> type, Type returnType, String methodName, Type... parameters)
	{
		Class<?> currType = type;
		while (currType != null)
		{
			Method method = getDeclaredMethodIntern(currType, returnType, methodName, parameters, true);
			if (method != null)
			{
				return method;
			}
			currType = currType.getSuperclass();
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException(type + " does not implement '" + methodName + "'");
	}

	public static Constructor<?>[] getDeclaredConstructors(Class<?> type)
	{
		return type.getDeclaredConstructors();
	}

	public static Constructor<?> getDeclaredConstructor(boolean tryOnly, Class<?> type, Type[] parameters)
	{
		for (Constructor<?> declaredMethod : getDeclaredConstructors(type))
		{
			Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
			if (parameterTypes.length != parameters.length)
			{
				continue;
			}
			boolean sameParams = true;
			for (int b = parameterTypes.length; b-- > 0;)
			{
				if (!Type.getType(parameterTypes[b]).equals(parameters[b]))
				{
					sameParams = false;
					break;
				}
			}
			if (sameParams)
			{
				return declaredMethod;
			}
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException("No matching constructor found");
	}

	public static final Field getDeclaredField(Class<?> type, String fieldName)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			HashMap<String, Field> nameToDeclaredFieldMap = entry.nameToDeclaredFieldMap;
			if (nameToDeclaredFieldMap != null)
			{
				return nameToDeclaredFieldMap.get(fieldName);
			}
			initDeclaredFields(type, entry);
			return entry.nameToDeclaredFieldMap.get(fieldName);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public static final Field[] getDeclaredFieldInHierarchy(Class<?> type, String fieldName)
	{
		writeLock.lock();
		try
		{
			ReflectEntry entry = getReflectEntry(type);
			HashMap<String, Field[]> nameToDeclaredFieldsInHierarchyMap = entry.nameToDeclaredFieldsInHierarchyMap;

			Field[] fields;
			if (nameToDeclaredFieldsInHierarchyMap != null)
			{
				fields = nameToDeclaredFieldsInHierarchyMap.get(fieldName);
			}
			else
			{
				initDeclaredFields(type, entry);
				fields = entry.nameToDeclaredFieldsInHierarchyMap.get(fieldName);
			}
			if (fields == null)
			{
				fields = EMPTY_FIELDS;
			}
			return fields;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected static final void initDeclaredFields(Class<?> type, ReflectEntry entry)
	{
		entry.nameToDeclaredFieldMap = new HashMap<String, Field>(0.5f);
		entry.nameToDeclaredFieldsInHierarchyMap = new HashMap<String, Field[]>(0.5f);
		ArrayList<Field> allDeclaredFields = new ArrayList<Field>();
		Field[] declaredFields = type.getDeclaredFields();
		for (Field declaredField : declaredFields)
		{
			declaredField.setAccessible(true);
			entry.nameToDeclaredFieldMap.put(declaredField.getName(), declaredField);
			allDeclaredFields.add(declaredField);
		}
		Class<?> currType = type.getSuperclass();
		if (currType != null && currType != Object.class)
		{
			Field[] currDeclaredFields = getDeclaredFieldsInHierarchy(currType);
			allDeclaredFields.addAll(currDeclaredFields);
		}
		entry.declaredFields = declaredFields;
		entry.declaredFieldsInHierarchy = allDeclaredFields.toArray(Field.class);

		for (Field declaredField : entry.declaredFieldsInHierarchy)
		{
			Field[] fieldsInHierarchy = entry.nameToDeclaredFieldsInHierarchyMap.get(declaredField.getName());
			if (fieldsInHierarchy == null)
			{
				fieldsInHierarchy = new Field[1];
			}
			else
			{
				Field[] newFieldsInHierarchy = new Field[fieldsInHierarchy.length + 1];
				System.arraycopy(fieldsInHierarchy, 0, newFieldsInHierarchy, 0, fieldsInHierarchy.length);
				fieldsInHierarchy = newFieldsInHierarchy;
			}
			entry.nameToDeclaredFieldsInHierarchyMap.put(declaredField.getName(), fieldsInHierarchy);
			fieldsInHierarchy[fieldsInHierarchy.length - 1] = declaredField;
		}
	}

	protected static void initDeclaredMethods(Class<?> type, ReflectEntry entry)
	{
		ArrayList<Method> declaredMethodsList = new ArrayList<Method>();
		ArrayList<Method> allDeclaredMethodsList = new ArrayList<Method>();
		fillDeclaredMethods(type, declaredMethodsList);
		Method[] declaredMethods = declaredMethodsList.toArray(Method.class);
		for (Method declaredMethod : declaredMethods)
		{
			declaredMethod.setAccessible(true);
			allDeclaredMethodsList.add(declaredMethod);
		}
		entry.declaredMethods = declaredMethods;

		Class<?> currType = type.getSuperclass();
		if (currType != null && currType != Object.class)
		{
			Method[] currDeclaredMethods = getDeclaredMethodsInHierarchy(currType);
			allDeclaredMethodsList.addAll(currDeclaredMethods);
		}
		for (Class<?> currInterface : type.getInterfaces())
		{
			Method[] currDeclaredMethods = getDeclaredMethodsInHierarchy(currInterface);
			allDeclaredMethodsList.addAll(currDeclaredMethods);
		}
		entry.declaredMethodsInHierarchy = allDeclaredMethodsList.toArray(Method.class);
	}

	private ReflectUtil()
	{
		// Intended blank
	}
}
