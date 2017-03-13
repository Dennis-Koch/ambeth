package com.koch.ambeth.service.typeinfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.FastConstructorAccess;
import com.koch.ambeth.util.typeinfo.NullEquivalentValueUtil;
import com.koch.ambeth.util.typeinfo.Transient;

public class FieldInfoItem extends TypeInfoItem
{
	protected Field field;

	protected String propertyName;

	protected String xmlName;

	protected Object defaultValue;

	protected Object nullEquivalentValue;

	@XmlElement(required = false)
	protected boolean xmlIgnore;

	protected boolean allowNullEquivalentValue;

	protected FastConstructorAccess<?> constructorOfRealType;

	public FieldInfoItem(Field field)
	{
		this(field, true);
	}

	public FieldInfoItem(Field field, boolean allowNullEquivalentValue)
	{
		this(field, allowNullEquivalentValue, field.getName());
	}

	public FieldInfoItem(Field field, String propertyName)
	{
		this(field, true, propertyName);
	}

	public FieldInfoItem(Field field, boolean allowNullEquivalentValue, String propertyName)
	{
		ParamChecker.assertParamNotNull(field, "field");
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		this.allowNullEquivalentValue = allowNullEquivalentValue;
		this.field = field;
		this.field.setAccessible(true);
		this.declaringType = field.getDeclaringClass();
		this.propertyName = propertyName;

		Class<?> fieldType = field.getType();
		Type genericFieldType = field.getGenericType();
		elementType = TypeInfoItemUtil.getElementTypeUsingReflection(fieldType, genericFieldType);
		if (fieldType.isPrimitive())
		{
			nullEquivalentValue = NullEquivalentValueUtil.getNullEquivalentValue(fieldType);
		}
		else if (Collection.class.isAssignableFrom(fieldType) && !fieldType.isInterface())
		{
			constructorOfRealType = FastConstructorAccess.get(fieldType);
		}
		Annotation annotation = field.getAnnotation(XmlElement.class);
		if (annotation != null)
		{
			xmlName = ((XmlElement) annotation).name();
		}
		if (xmlName == null || xmlName.isEmpty() || "##default".equals(xmlName))
		{
			xmlName = getName();
		}
		xmlIgnore = false;

		if (field.getAnnotation(Transient.class) != null || field.getAnnotation(XmlTransient.class) != null || Modifier.isTransient(field.getModifiers())
				|| Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
		{
			xmlIgnore = true;
		}
	}

	@Override
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	protected FastConstructorAccess<?> getConstructorOfRealType()
	{
		return constructorOfRealType;
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return nullEquivalentValue;
	}

	@Override
	public void setNullEquivalentValue(Object nullEquivalentValue)
	{
		this.nullEquivalentValue = nullEquivalentValue;
	}

	public Field getField()
	{
		return field;
	}

	@Override
	public Class<?> getRealType()
	{
		return field.getType();
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		try
		{
			if (value == null && allowNullEquivalentValue)
			{
				value = nullEquivalentValue;
			}
			field.set(obj, value);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object getValue(Object obj)
	{
		return this.getValue(obj, allowNullEquivalentValue);
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object value = null;
		try
		{
			value = field.get(obj);
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
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return field.getAnnotation(annotationType);
	}

	@Override
	public String getName()
	{
		return propertyName;
	}

	@Override
	public String getXMLName()
	{
		return xmlName;
	}

	@Override
	public boolean isXMLIgnore()
	{
		return xmlIgnore;
	}

	@Override
	public String toString()
	{
		return "Field " + getName() + "/" + getXMLName();
	}

}
