package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.annotation.MBeanOperation;
import com.koch.ambeth.ioc.util.IImmutableTypeSet;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

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
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractBeanMonitoringSupport implements DynamicMBean {
    protected final Object bean;

    public AbstractBeanMonitoringSupport(Object bean) {
        super();
        this.bean = bean;
    }

    protected abstract IImmutableTypeSet getImmutableTypeSet();

    protected abstract IPropertyInfoProvider getPropertyInfoProvider();

    protected abstract IConversionHelper getConversionHelper();

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        var propertyInfoProvider = getPropertyInfoProvider();
        var propertyInfo = propertyInfoProvider.getProperty(bean.getClass(), attribute);
        var value = propertyInfo.getValue(bean);
        return convertValue(value);
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        var propertyInfoProvider = getPropertyInfoProvider();
        var propertyInfo = propertyInfoProvider.getProperty(bean.getClass(), attribute.getName());
        var value = getConversionHelper().convertValueToType(propertyInfo.getPropertyType(), attribute.getValue());
        propertyInfo.setValue(bean, value);
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        var propertyInfoProvider = getPropertyInfoProvider();
        var properties = propertyInfoProvider.getProperties(bean.getClass());
        var list = new AttributeList(properties.length);
        for (int a = 0, size = properties.length; a < size; a++) {
            var propertyInfo = properties[a];
            list.add(createAttribute(propertyInfo.getName(), propertyInfo.getValue(bean)));
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        var propertyInfoProvider = getPropertyInfoProvider();
        var conversionHelper = getConversionHelper();
        var propertyMap = propertyInfoProvider.getPropertyMap(bean.getClass());
        var properties = propertyInfoProvider.getProperties(bean.getClass());

        for (int a = 0, size = attributes.size(); a < size; a++) {
            var attribute = (Attribute) attributes.get(a);
            var propertyInfo = propertyMap.get(attribute.getName());
            var value = conversionHelper.convertValueToType(propertyInfo.getPropertyType(), attribute.getValue());
            propertyInfo.setValue(bean, value);
        }

        var list = new AttributeList(properties.length);

        for (int a = 0, size = attributes.size(); a < size; a++) {
            var attribute = (Attribute) attributes.get(a);
            var propertyInfo = propertyMap.get(attribute.getName());
            var value = propertyInfo.getValue(bean);
            list.add(createAttribute(attribute.getName(), value));
        }
        return list;
    }

    protected Attribute createAttribute(String name, Object value) {
        return new Attribute(name, convertValue(value));
    }

    protected Object convertValue(Object value) {
        if (value instanceof Class) {
            return ((Class<?>) value).getName();
        } else if (value != null && value.getClass().isEnum()) {
            return value.toString();
        }
        return value;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        var method = ReflectUtil.getDeclaredMethod(false, bean.getClass(), null, actionName, (Class<?>[]) null);
        try {
            return method.invoke(bean, params);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(((Exception) RuntimeExceptionUtil.mask(e.getCause(), Exception.class)));
        } catch (Throwable e) {
            throw new ReflectionException(((Exception) RuntimeExceptionUtil.mask(e, Exception.class)));
        }
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        var propertyInfoProvider = getPropertyInfoProvider();
        var properties = propertyInfoProvider.getProperties(bean.getClass());
        var immutableTypeSet = getImmutableTypeSet();

        var methods = ReflectUtil.getDeclaredMethods(bean.getClass());
        ArrayList<MBeanOperationInfo> operations = null;
        ArrayList<MBeanAttributeInfo> attributes = null;
        for (int a = methods.length; a-- > 0; ) {
            var method = methods[a];
            if (!method.isAnnotationPresent(MBeanOperation.class)) {
                continue;
            }
            if (operations == null) {
                operations = new ArrayList<>();
            }
            operations.add(new MBeanOperationInfo(method.getName(), method));
        }
        for (int a = properties.length; a-- > 0; ) {
            var propertyInfo = properties[a];
            if (!propertyInfo.isReadable() || propertyInfo.isAnnotationPresent(MBeanOperation.class)) {
                continue;
            }
            if (!immutableTypeSet.isImmutableType(propertyInfo.getPropertyType())) {
                continue;
            }
            if (attributes == null) {
                attributes = new ArrayList<>(a + 1);
            }
            attributes.add(new MBeanAttributeInfo(propertyInfo.getName(), propertyInfo.getPropertyType().getName(), null, propertyInfo.isReadable(), propertyInfo.isWritable(), false));
        }
        var info = new MBeanInfo(bean.getClass().getName(), null, attributes != null ? attributes.toArray(MBeanAttributeInfo[]::new) : null, null,
                operations != null ? operations.toArray(MBeanOperationInfo[]::new) : null, null, null);
        return info;
    }
}
