package de.osthus.ambeth.typeinfo;

import java.lang.reflect.Field;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;

public class FieldInfoItemASM extends FieldInfoItem
{
	protected final FieldAccess fieldAccess;

	protected final int fieldIndex;

	public FieldInfoItemASM(Field field, FieldAccess fieldAccess)
	{
		this(field, true, fieldAccess);
	}

	public FieldInfoItemASM(Field field, boolean allowNullEquivalentValue, FieldAccess fieldAccess)
	{
		this(field, allowNullEquivalentValue, field.getName(), fieldAccess);
	}

	public FieldInfoItemASM(Field field, String propertyName, FieldAccess fieldAccess)
	{
		this(field, true, propertyName, fieldAccess);
	}

	public FieldInfoItemASM(Field field, boolean allowNullEquivalentValue, String propertyName, FieldAccess fieldAccess)
	{
		super(field, allowNullEquivalentValue, propertyName);
		this.fieldAccess = fieldAccess;
		fieldIndex = fieldAccess.getIndex(field.getName());
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object value = null;
		try
		{
			value = fieldAccess.get(obj, fieldIndex);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		Object nullEquivalentValue = this.nullEquivalentValue;
		if (nullEquivalentValue != null && nullEquivalentValue.equals(value))
		{
			if (allowNullEquivalentValue)
			{
				return nullEquivalentValue;
			}
			return null;
		}
		return value;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		if (value == null && allowNullEquivalentValue)
		{
			value = nullEquivalentValue;
		}
		fieldAccess.set(obj, fieldIndex, value);
	}
}
