package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Method;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;

public class MethodPropertyInfoASM extends MethodPropertyInfo
{
	protected final MethodAccess methodAccess;

	protected final int getterIndex, setterIndex;

	public MethodPropertyInfoASM(Class<?> entityType, String propertyName, Method getter, Method setter, IThreadLocalObjectCollector objectCollector,
			MethodAccess methodAccess)
	{
		super(entityType, propertyName, getter, setter, objectCollector);
		this.methodAccess = methodAccess;
		getterIndex = getter != null ? methodAccess.getIndex(getter.getName(), getter.getParameterTypes()) : -1;
		setterIndex = setter != null ? methodAccess.getIndex(setter.getName(), setter.getParameterTypes()) : -1;
		this.readable = getterIndex != -1;
		this.writable = setterIndex != -1;
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
