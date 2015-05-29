package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public class FieldPropertyInfo extends AbstractPropertyInfo
{
	protected boolean writable;

	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field)
	{
		this(entityType, propertyName, field, null);
	}

	public FieldPropertyInfo(Class<?> entityType, String propertyName, Field field, IThreadLocalObjectCollector objectCollector)
	{
		super(entityType, objectCollector);
		field.setAccessible(true);
		backingField = field;
		name = propertyName;
		declaringType = field.getDeclaringClass();
		propertyType = field.getType();
		elementType = TypeInfoItemUtil.getElementTypeUsingReflection(propertyType, field.getGenericType());
		init(objectCollector);
	}

	@Override
	protected void init(IThreadLocalObjectCollector objectCollector)
	{
		putAnnotations(backingField);
		super.init(objectCollector);

		writable = (Modifier.isPublic(backingField.getModifiers()) || Modifier.isProtected(backingField.getModifiers()))
				&& !Modifier.isFinal(backingField.getModifiers());
	}

	@Override
	public boolean isReadable()
	{
		return true;
	}

	@Override
	public boolean isWritable()
	{
		return writable;
	}

	@Override
	public boolean isFieldWritable()
	{
		return writable;
	}

	@Override
	public Object getValue(Object obj)
	{
		try
		{
			return backingField.get(obj);
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
			backingField.set(obj, value);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
