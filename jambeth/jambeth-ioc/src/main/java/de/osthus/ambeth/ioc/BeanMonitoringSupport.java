package de.osthus.ambeth.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.MBeanOperation;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.util.ReflectUtil;

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
		Method method = ReflectUtil.getDeclaredMethod(false, bean.getClass(), null, actionName, (Class<?>[]) null);
		try
		{
			return method.invoke(bean, params);
		}
		catch (InvocationTargetException e)
		{
			throw new ReflectionException(((Exception) RuntimeExceptionUtil.mask(e.getCause(), Exception.class)));
		}
		catch (Throwable e)
		{
			throw new ReflectionException(((Exception) RuntimeExceptionUtil.mask(e, Exception.class)));
		}
	}

	@Override
	public MBeanInfo getMBeanInfo()
	{
		IPropertyInfoProvider propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(bean.getClass());

		Method[] methods = ReflectUtil.getDeclaredMethods(bean.getClass());
		ArrayList<MBeanOperationInfo> operations = null;
		ArrayList<MBeanAttributeInfo> attributes = null;
		for (int a = methods.length; a-- > 0;)
		{
			Method method = methods[a];
			if (!method.isAnnotationPresent(MBeanOperation.class))
			{
				continue;
			}
			if (operations == null)
			{
				operations = new ArrayList<MBeanOperationInfo>();
			}
			operations.add(new MBeanOperationInfo(method.getName(), method));
		}
		for (int a = properties.length; a-- > 0;)
		{
			IPropertyInfo propertyInfo = properties[a];
			if (!propertyInfo.isReadable() || propertyInfo.isAnnotationPresent(MBeanOperation.class))
			{
				continue;
			}
			if (!ImmutableTypeSet.isImmutableType(propertyInfo.getPropertyType()))
			{
				continue;
			}
			if (attributes == null)
			{
				attributes = new ArrayList<MBeanAttributeInfo>(a + 1);
			}
			attributes.add(new MBeanAttributeInfo(propertyInfo.getName(), propertyInfo.getPropertyType().getName(), null, propertyInfo.isReadable(),
					propertyInfo.isWritable(), false));
		}
		MBeanInfo info = new MBeanInfo(bean.getClass().getName(), null, attributes != null ? attributes.toArray(MBeanAttributeInfo.class) : null, null,
				operations != null ? operations.toArray(MBeanOperationInfo.class) : null, null, null);
		return info;
	}
}
