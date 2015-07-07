package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class MethodPropertyInfo extends AbstractPropertyInfo
{
	protected static final Object[] EMPTY_ARGS = new Object[0];

	protected Method getter, setter;

	protected boolean writable, readable;

	public MethodPropertyInfo(Class<?> entityType, String propertyName, Method getter, Method setter)
	{
		this(entityType, propertyName, getter, setter, null);
	}

	public MethodPropertyInfo(Class<?> entityType, String propertyName, Method getter, Method setter, IThreadLocalObjectCollector objectCollector)
	{
		super(entityType, objectCollector);
		name = propertyName;
		if (name.isEmpty())
		{
			throw new RuntimeException("Not a property method: " + entityType.getName() + "." + getter.getName());
		}
		if (getter != null)
		{
			ParamChecker.assertTrue(!void.class.equals(getter.getReturnType()), "getter");
			ParamChecker.assertTrue(getter.getParameterTypes().length == 0, "getter");
		}
		if (setter != null)
		{
			ParamChecker.assertTrue(setter.getParameterTypes().length == 1, "setter");
		}
		this.getter = getter;
		this.setter = setter;
		writable = this.setter != null && (Modifier.isPublic(this.setter.getModifiers()) || Modifier.isProtected(this.setter.getModifiers()));
		readable = this.getter != null && (Modifier.isPublic(this.getter.getModifiers()) || Modifier.isProtected(this.getter.getModifiers()));
		init(objectCollector);
	}

	@Override
	protected void init(IThreadLocalObjectCollector objectCollector)
	{
		if (entityType == null)
		{
			throw new IllegalArgumentException("No class given");
		}
		if (getter == null && setter == null)
		{
			throw new IllegalArgumentException("No property methods (class is '" + entityType + "')");
		}
		else if (getter != null)
		{
			Class<?> declaringClass = getter.getDeclaringClass();
			propertyType = getter.getReturnType();
			Type returnType = getter.getGenericReturnType();
			elementType = TypeInfoItemUtil.getElementTypeUsingReflection(propertyType, returnType);

			String nameLower = name.toLowerCase();
			Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(declaringClass);
			Field backingField = null, weakBackingField = null;
			for (int a = fields.length; a-- > 0;)
			{
				Field field = fields[a];
				int fieldModifiers = field.getModifiers();
				if (Modifier.isStatic(fieldModifiers))
				{
					continue;
				}
				String fieldName = field.getName().toLowerCase();
				if (fieldName.equals(nameLower))
				{
					backingField = field;
					break;
				}
				else if (fieldName.endsWith(nameLower))
				{
					if (weakBackingField != null)
					{
						weakBackingField = null;
						break;
					}
					weakBackingField = field;
				}
			}
			if (backingField == null)
			{
				backingField = weakBackingField;
			}
			this.backingField = backingField;
			if (backingField != null)
			{
				modifiers = backingField.getModifiers();
				putAnnotations(backingField);
			}
			putAnnotations(getter);
			if (setter != null)
			{
				if (setter.getParameterTypes().length != 1 || !setter.getParameterTypes()[0].equals(propertyType))
				{
					throw new RuntimeException("Misfitting property methods for property '" + name + "' on class '" + entityType.getName() + "'");
				}
				putAnnotations(setter);
			}
		}
		else
		{
			Class<?> declaringClass = setter.getDeclaringClass();
			Field[] fields = ReflectUtil.getDeclaredFieldsInHierarchy(declaringClass);
			for (int a = fields.length; a-- > 0;)
			{
				Field field = fields[a];
				int fieldModifiers = field.getModifiers();
				if (Modifier.isFinal(fieldModifiers) || Modifier.isStatic(fieldModifiers))
				{
					continue;
				}
				String fieldName = field.getName();
				if (fieldName.endsWith(name))
				{
					if (backingField != null)
					{
						backingField = null;
						break;
					}
					backingField = field;
				}
			}
			if (backingField != null)
			{
				modifiers = backingField.getModifiers();
			}
			propertyType = setter.getParameterTypes()[0];
			Type[] paramTypes = setter.getGenericParameterTypes();
			elementType = TypeInfoItemUtil.getElementTypeUsingReflection(propertyType, paramTypes[0]);
			putAnnotations(setter);
		}
		if (getter != null && Modifier.isNative(getter.getModifiers()))
		{
			modifiers |= Modifier.NATIVE;
		}
		if (setter != null && Modifier.isNative(setter.getModifiers()))
		{
			modifiers |= Modifier.NATIVE;
		}
		if (setter != null)
		{
			modifiers &= ~Modifier.FINAL;
		}
		Boolean getterIsPublic = getter != null ? Boolean.valueOf(Modifier.isPublic(getter.getModifiers())) : null;
		Boolean setterIsPublic = setter != null ? Boolean.valueOf(Modifier.isPublic(setter.getModifiers())) : null;
		if (Boolean.TRUE.equals(getterIsPublic) || Boolean.TRUE.equals(setterIsPublic))
		{
			modifiers &= ~Modifier.PRIVATE;
			modifiers &= ~Modifier.PROTECTED;
			modifiers |= Modifier.PUBLIC;
		}
		else
		{
			Boolean getterIsProtected = getter != null ? Boolean.valueOf(Modifier.isProtected(getter.getModifiers())) : null;
			Boolean setterIsProtected = setter != null ? Boolean.valueOf(Modifier.isProtected(setter.getModifiers())) : null;
			if (Boolean.TRUE.equals(getterIsProtected) || Boolean.TRUE.equals(setterIsProtected))
			{
				modifiers &= ~Modifier.PRIVATE;
				modifiers &= ~Modifier.PUBLIC;
				modifiers |= Modifier.PROTECTED;
			}
			else
			{
				modifiers &= ~Modifier.PROTECTED;
				modifiers &= ~Modifier.PUBLIC;
				modifiers |= Modifier.PRIVATE;
			}
		}

		refreshDeclaringType();
		super.init(objectCollector);
	}

	protected void refreshDeclaringType()
	{
		Class<?> fieldDC = backingField != null ? backingField.getDeclaringClass() : null;
		Class<?> getterDC = getter != null ? getter.getDeclaringClass() : null;
		Class<?> setterDC = setter != null ? setter.getDeclaringClass() : null;
		Class<?> mostSpecificDC = fieldDC;
		if (mostSpecificDC == null)
		{
			mostSpecificDC = getterDC;
		}
		else if (getterDC != null)
		{
			mostSpecificDC = getterDC.isAssignableFrom(mostSpecificDC) ? mostSpecificDC : getterDC;
		}
		if (mostSpecificDC == null)
		{
			mostSpecificDC = setterDC;
		}
		else if (setterDC != null)
		{
			mostSpecificDC = setterDC.isAssignableFrom(mostSpecificDC) ? mostSpecificDC : setterDC;
		}
		declaringType = mostSpecificDC;
	}

	@Override
	public void refreshAccessors(Class<?> realType)
	{
		super.refreshAccessors(realType);
		getter = ReflectUtil.getDeclaredMethod(true, realType, getPropertyType(), "get" + getName());
		if (getter == null)
		{
			getter = ReflectUtil.getDeclaredMethod(true, realType, getPropertyType(), "is" + getName());
		}
		setter = ReflectUtil.getDeclaredMethod(true, realType, null, "set" + getName(), getPropertyType());
		writable = setter != null && (Modifier.isPublic(setter.getModifiers()) || Modifier.isProtected(setter.getModifiers()));
		readable = getter != null && (Modifier.isPublic(getter.getModifiers()) || Modifier.isProtected(getter.getModifiers()));
		refreshDeclaringType();
	}

	@Override
	public boolean isWritable()
	{
		return writable;
	}

	@Override
	public boolean isReadable()
	{
		return readable;
	}

	public Method getGetter()
	{
		return getter;
	}

	public Method getSetter()
	{
		return setter;
	}

	@Override
	public Object getValue(Object obj)
	{
		if (getter == null)
		{
			return null;
		}
		try
		{
			return getter.invoke(obj, EMPTY_ARGS);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while calling '" + getter + "' on object '" + obj + "' of type '" + obj.getClass().toString()
					+ "'");
		}
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		if (setter == null)
		{
			throw new UnsupportedOperationException("No setter configured for property " + name);
		}
		Object[] args = { value };
		try
		{
			setter.invoke(obj, args);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while calling '" + setter + "' on object '" + obj + "' of type '" + obj.getClass().toString()
					+ "' with argument '" + value + "'");
		}
	}
}
