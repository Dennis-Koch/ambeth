package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;

public class FieldPropertyInfoASM extends FieldPropertyInfo
{
	protected final FieldAccess fieldAccess;

	protected final int index;

	public FieldPropertyInfoASM(Class<?> entityType, String propertyName, Field field, IThreadLocalObjectCollector objectCollector, FieldAccess fieldAccess)
	{
		super(entityType, propertyName, field, objectCollector);
		this.fieldAccess = fieldAccess;
		index = fieldAccess.getIndex(field.getName());
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		try
		{
			fieldAccess.set(obj, index, value);
		}
		catch (Throwable e)
		{
			if (index == -1)
			{
				throw new UnsupportedOperationException("No setter mapped while calling property '" + getName() + "' on object '" + obj + "' of type '"
						+ obj.getClass().toString() + "' with argument '" + value + "'");
			}
			throw RuntimeExceptionUtil.mask(e, "Error occured while setting '" + backingField + "' on object '" + obj + "' of type '"
					+ obj.getClass().toString() + "' with argument '" + value + "'");
		}
	}

	@Override
	public Object getValue(Object obj)
	{
		try
		{
			return fieldAccess.get(obj, index);
		}
		catch (Throwable e)
		{
			if (index == -1)
			{
				throw new UnsupportedOperationException();
			}
			throw RuntimeExceptionUtil.mask(e, "Error occured while getting '" + backingField + "' on object '" + obj + "' of type '"
					+ obj.getClass().toString() + "'");
		}
	}
}
