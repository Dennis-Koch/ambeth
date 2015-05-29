package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;

public class MethodPropertyInfoASM extends MethodPropertyInfo
{
	protected MethodAccess methodAccess;

	protected FieldAccess fieldAccess;

	protected int getterIndex, setterIndex;

	protected int fieldIndex;

	public MethodPropertyInfoASM(Class<?> entityType, String propertyName, Method getter, Method setter, IThreadLocalObjectCollector objectCollector,
			MethodAccess methodAccess)
	{
		super(entityType, propertyName, getter, setter, objectCollector);
		this.methodAccess = methodAccess;
		getterIndex = getter != null ? methodAccess.getIndex(getter.getName(), getter.getParameterTypes()) : -1;
		setterIndex = setter != null ? methodAccess.getIndex(setter.getName(), setter.getParameterTypes()) : -1;
		readable = getterIndex != -1;
		writable = setterIndex != -1;
		fieldWritable = writable;
		if (!fieldWritable && backingField != null && !Modifier.isFinal(backingField.getModifiers()))
		{
			fieldWritable = true;
			fieldAccess = FieldAccess.get(entityType);
			fieldIndex = fieldAccess.getIndex(backingField.getName());
		}
	}

	@Override
	public void refreshAccessors(Class<?> realType)
	{
		super.refreshAccessors(realType);
		methodAccess = MethodAccess.get(realType);
		getterIndex = getter != null ? methodAccess.getIndex(getter.getName(), getter.getParameterTypes()) : -1;
		setterIndex = setter != null ? methodAccess.getIndex(setter.getName(), setter.getParameterTypes()) : -1;
		readable = getterIndex != -1;
		writable = setterIndex != -1;
		fieldWritable = writable;
		if (!fieldWritable && backingField != null && !Modifier.isFinal(backingField.getModifiers()))
		{
			fieldWritable = true;
			fieldAccess = FieldAccess.get(backingField.getDeclaringClass());
			fieldIndex = fieldAccess.getIndex(backingField.getName());
		}
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		if (setterIndex == -1 && fieldAccess != null)
		{
			fieldAccess.set(obj, fieldIndex, value);
			return;
		}
		try
		{
			methodAccess.invoke(obj, setterIndex, value);
		}
		catch (Throwable e)
		{
			if (setterIndex == -1)
			{
				throw new UnsupportedOperationException("No setter mapped while calling property '" + getName() + "' on object '" + obj + "' of type '"
						+ obj.getClass().toString() + "' with argument '" + value + "'");
			}
			throw RuntimeExceptionUtil.mask(e, "Error occured while calling '" + setter + "' on object '" + obj + "' of type '" + obj.getClass().toString()
					+ "' with argument '" + value + "'");
		}
	}

	@Override
	public Object getValue(Object obj)
	{
		if (getterIndex == -1 && fieldAccess != null)
		{
			return fieldAccess.get(obj, fieldIndex);
		}
		try
		{
			return methodAccess.invoke(obj, getterIndex, EMPTY_ARGS);
		}
		catch (Throwable e)
		{
			if (getterIndex == -1)
			{
				throw new UnsupportedOperationException();
			}
			throw RuntimeExceptionUtil.mask(e, "Error occured while calling '" + getter + "' on object '" + obj + "' of type '" + obj.getClass().toString()
					+ "'");
		}
	}
}
