package com.koch.ambeth.ioc.typeinfo;

import java.lang.reflect.Method;

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class MethodPropertyInfoASM extends MethodPropertyInfo
{
	protected MethodAccess methodAccess;

	protected int getterIndex, setterIndex;

	public MethodPropertyInfoASM(Class<?> entityType, String propertyName, Method getter, Method setter, IThreadLocalObjectCollector objectCollector,
			MethodAccess methodAccess)
	{
		super(entityType, propertyName, getter, setter, objectCollector);
		this.methodAccess = methodAccess;
		getterIndex = getter != null ? methodAccess.getIndex(getter.getName(), getter.getParameterTypes()) : -1;
		setterIndex = setter != null ? methodAccess.getIndex(setter.getName(), setter.getParameterTypes()) : -1;
		readable = getterIndex != -1;
		writable = setterIndex != -1;
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
	}

	@Override
	public void setValue(Object obj, Object value)
	{
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
