package de.osthus.ambeth.accessor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.util.ReflectUtil;

public class DefaultAccessor extends AbstractAccessor
{
	private static final Object[] EMPTY_ARGS = new Object[0];

	protected final Method getter, setter;

	protected final boolean readable, writable;

	public DefaultAccessor(Class<?> type, String propertyName, Class<?> propertyType)
	{
		super(type, propertyName, propertyType);
		Method getter = ReflectUtil.getDeclaredMethod(true, type, propertyType, "get" + propertyName);
		if (getter == null)
		{
			getter = ReflectUtil.getDeclaredMethod(true, type, propertyType, "is" + propertyName);
		}
		this.getter = getter;
		setter = ReflectUtil.getDeclaredMethod(true, type, null, "set" + propertyName, propertyType);
		readable = getter != null && Modifier.isPublic(getter.getModifiers());
		writable = setter != null && Modifier.isPublic(setter.getModifiers());
	}

	@Override
	public boolean canRead()
	{
		return readable;
	}

	@Override
	public boolean canWrite()
	{
		return writable;
	}

	@Override
	public Object getValue(Object obj)
	{
		try
		{
			return getter.invoke(obj, EMPTY_ARGS);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		try
		{
			setter.invoke(obj, value);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
