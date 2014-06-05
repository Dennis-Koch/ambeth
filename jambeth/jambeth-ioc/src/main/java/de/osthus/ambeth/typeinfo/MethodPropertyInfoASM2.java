package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Method;

import de.osthus.ambeth.accessor.AbstractAccessor;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public class MethodPropertyInfoASM2 extends MethodPropertyInfo
{
	protected AbstractAccessor accessor;

	public MethodPropertyInfoASM2(Class<?> entityType, String propertyName, Method getter, Method setter, IThreadLocalObjectCollector objectCollector,
			AbstractAccessor accessor)
	{
		super(entityType, propertyName, getter, setter, objectCollector);
		setAccessor(accessor);
	}

	public void setAccessor(AbstractAccessor accessor)
	{
		this.accessor = accessor;
		readable = accessor.canRead();
		writable = accessor.canWrite();
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		accessor.setValue(obj, value);
	}

	@Override
	public Object getValue(Object obj)
	{
		return accessor.getValue(obj);
	}
}
