package de.osthus.ambeth.typeinfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class PropertyInfoItem extends RelationInfoItem
{
	protected IPropertyInfoIntern property;

	protected String xmlName;

	protected Object defaultValue;

	protected Object nullEquivalentValue;

	@XmlElement(required = false)
	protected boolean xmlIgnore;

	protected boolean allowNullEquivalentValue;

	protected FastConstructorAccess<?> constructorOfRealType;

	public PropertyInfoItem()
	{
		// intended blank
	}

	public PropertyInfoItem(IPropertyInfo property)
	{
		this(property, true);
	}

	public PropertyInfoItem(IPropertyInfo property, boolean allowNullEquivalentValue)
	{
		this.property = (IPropertyInfoIntern) property;
		this.allowNullEquivalentValue = allowNullEquivalentValue;
		declaringType = property.getDeclaringType();
		elementType = property.getElementType();
		Class<?> propertyType = property.getPropertyType();
		if (propertyType.isPrimitive())
		{
			nullEquivalentValue = NullEquivalentValueUtil.getNullEquivalentValue(propertyType);
		}
		else if (Collection.class.isAssignableFrom(propertyType) && !propertyType.isInterface())
		{
			constructorOfRealType = FastConstructorAccess.get(propertyType);
		}
		XmlElement annotation = property.getAnnotation(XmlElement.class);
		if (annotation != null)
		{
			xmlName = annotation.name();
		}
		if (xmlName == null || xmlName.isEmpty() || "##default".equals(xmlName))
		{
			xmlName = getName();
		}
		xmlIgnore = false;

		if (property.getAnnotation(Transient.class) != null || property.getAnnotation(XmlTransient.class) != null
				|| Modifier.isTransient(property.getModifiers()) || Modifier.isFinal(property.getModifiers()) || Modifier.isStatic(property.getModifiers())
				|| !canRead() || !canWrite())
		{
			xmlIgnore = true;
		}
	}

	public boolean isAllowNullEquivalentValue()
	{
		return allowNullEquivalentValue;
	}

	public void setAllowNullEquivalentValue(boolean allowNullEquivalentValue)
	{
		this.allowNullEquivalentValue = allowNullEquivalentValue;
	}

	@Override
	protected FastConstructorAccess<?> getConstructorOfRealType()
	{
		return constructorOfRealType;
	}

	public IPropertyInfo getProperty()
	{
		return property;
	}

	public void setProperty(IPropertyInfoIntern property)
	{
		this.property = property;
	}

	@Override
	public void setElementType(Class<?> elementType)
	{
		super.setElementType(elementType);
		property.setElementType(elementType);
	}

	@Override
	public Class<?> getRealType()
	{
		return property.getPropertyType();
	}

	@Override
	public boolean canRead()
	{
		return property.isReadable();
	}

	@Override
	public boolean canWrite()
	{
		return property.isWritable();
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
	public Object getNullEquivalentValue()
	{
		return nullEquivalentValue;
	}

	@Override
	public void setNullEquivalentValue(Object nullEquivalentValue)
	{
		this.nullEquivalentValue = nullEquivalentValue;
	}

	@Override
	public Object getValue(Object obj)
	{
		return getValue(obj, allowNullEquivalentValue);
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
			property.setValue(obj, value);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object value;
		try
		{
			value = property.getValue(obj);
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
		return property.getAnnotation(annotationType);
	}

	@Override
	public String getName()
	{
		return property.getName();
	}

	@Override
	public String getXMLName()
	{
		return xmlName;
	}

	@Override
	public Class<?> getElementType()
	{
		return property.getElementType();
	}

	@Override
	public boolean isXMLIgnore()
	{
		return xmlIgnore;
	}

	@Override
	public String toString()
	{
		return "Property " + getName() + "/" + getXMLName();
	}
}
