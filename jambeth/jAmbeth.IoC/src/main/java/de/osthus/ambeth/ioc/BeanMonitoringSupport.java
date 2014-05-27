package de.osthus.ambeth.ioc;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;

public class BeanMonitoringSupport implements DynamicMBean
{
	private Object bean;

	private IServiceContext beanContext;

	public BeanMonitoringSupport(Object bean, IServiceContext beanContext)
	{
		super();
		this.bean = bean;
		this.beanContext = beanContext;
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
	{
		IPropertyInfoProvider propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		IPropertyInfo propertyInfo = propertyInfoProvider.getProperty(bean.getClass(), attribute);
		Object value = propertyInfo.getValue(bean);
		return convertValue(value);
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
	{
		IPropertyInfoProvider propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		IPropertyInfo propertyInfo = propertyInfoProvider.getProperty(bean.getClass(), attribute.getName());
		Object value = beanContext.getService(IConversionHelper.class).convertValueToType(propertyInfo.getPropertyType(), attribute.getValue());
		propertyInfo.setValue(bean, value);
	}

	@Override
	public AttributeList getAttributes(String[] attributes)
	{
		IPropertyInfoProvider propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(bean.getClass());
		AttributeList list = new AttributeList(properties.length);
		for (int a = 0, size = properties.length; a < size; a++)
		{
			IPropertyInfo propertyInfo = properties[a];
			list.add(createAttribute(propertyInfo.getName(), propertyInfo.getValue(bean)));
		}
		return list;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes)
	{
		IPropertyInfoProvider propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		IConversionHelper conversionHelper = beanContext.getService(IConversionHelper.class);
		IMap<String, IPropertyInfo> propertyMap = propertyInfoProvider.getPropertyMap(bean.getClass());
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(bean.getClass());

		for (int a = 0, size = attributes.size(); a < size; a++)
		{
			Attribute attribute = (Attribute) attributes.get(a);
			IPropertyInfo propertyInfo = propertyMap.get(attribute.getName());
			Object value = conversionHelper.convertValueToType(propertyInfo.getPropertyType(), attribute.getValue());
			propertyInfo.setValue(bean, value);
		}

		AttributeList list = new AttributeList(properties.length);

		for (int a = 0, size = attributes.size(); a < size; a++)
		{
			Attribute attribute = (Attribute) attributes.get(a);
			IPropertyInfo propertyInfo = propertyMap.get(attribute.getName());
			Object value = propertyInfo.getValue(bean);
			list.add(createAttribute(attribute.getName(), value));
		}
		return list;
	}

	protected Attribute createAttribute(String name, Object value)
	{
		return new Attribute(name, convertValue(value));
	}

	protected Object convertValue(Object value)
	{
		if (value instanceof Class)
		{
			return ((Class<?>) value).getName();
		}
		else if (value != null && value.getClass().isEnum())
		{
			return value.toString();
		}
		return value;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
	{
		// ReflectUtil.getDeclaredMethod(false, bean.getClass(), actionName, )
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo()
	{
		IPropertyInfoProvider propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(bean.getClass());
		ArrayList<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>(properties.length);
		for (int a = properties.length; a-- > 0;)
		{
			IPropertyInfo propertyInfo = properties[a];
			if (!propertyInfo.isReadable())
			{
				continue;
			}
			if (!ImmutableTypeSet.isImmutableType(propertyInfo.getPropertyType()))
			{
				continue;
			}
			attributes.add(new MBeanAttributeInfo(propertyInfo.getName(), propertyInfo.getPropertyType().getName(), null, propertyInfo.isReadable(),
					propertyInfo.isWritable(), false));
		}
		MBeanInfo info = new MBeanInfo(bean.getClass().getName(), null, attributes.toArray(MBeanAttributeInfo.class), null, null, null, null);
		return info;
	}
}
