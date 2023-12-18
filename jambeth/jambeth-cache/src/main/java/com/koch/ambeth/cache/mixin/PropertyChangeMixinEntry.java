package com.koch.ambeth.cache.mixin;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.koch.ambeth.service.typeinfo.PropertyInfoItem;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.annotation.IgnoreToBeUpdated;
import com.koch.ambeth.util.annotation.ParentChild;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.model.IEmbeddedType;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class PropertyChangeMixinEntry {
    public static final String parentPropertyName = "Parent";

    public static final String parentFieldName = "f_" + parentPropertyName;

    public final String propertyName;

    public final String javaBeansPropertyName;

    public final ITypeInfoItem getDelegate;

    public final boolean doesModifyToBeUpdated;

    public final boolean firesToBeCreatedPCE;

    public final String[] propertyNames;

    public final Object[] unknownValues;

    public final boolean isParentChildSetter;

    public final boolean isAddedRemovedCheckNecessary;

    public final Boolean includeNewValue, includeOldValue;

    public PropertyChangeMixinEntry(Class<?> type, String propertyName, String javaBeansPropertyName, IPropertyInfoProvider propertyInfoProvider) {
        this.propertyName = propertyName;
        this.javaBeansPropertyName = javaBeansPropertyName;
        LinkedHashSet<String> propertyNames = new LinkedHashSet<>();
        propertyNames.add(propertyName);
        IPropertyInfo prop = propertyInfoProvider.getProperty(type, propertyName);
        PropertyChangeAspect propertyChangeAspect = findAnnotation(type, PropertyChangeAspect.class, true);
        if (propertyChangeAspect != null) {
            includeNewValue = propertyChangeAspect.includeNewValue();
            includeOldValue = propertyChangeAspect.includeOldValue();
        } else {
            includeNewValue = null;
            includeOldValue = null;
        }
        doesModifyToBeUpdated = (IDataObject.class.isAssignableFrom(type) || IEmbeddedType.class.isAssignableFrom(type)) && !prop.isAnnotationPresent(IgnoreToBeUpdated.class);
        isParentChildSetter = IDataObject.class.isAssignableFrom(type) && prop.isAnnotationPresent(ParentChild.class);
        isAddedRemovedCheckNecessary = !prop.getPropertyType().isPrimitive() && WrapperTypeSet.getUnwrappedType(prop.getPropertyType()) == null && !String.class.equals(prop.getPropertyType());

        PropertyChangeMixin.evaluateDependentProperties(type, prop, propertyNames, propertyInfoProvider);

        while (true) {
            int startCount = propertyNames.size();

            for (String currPropertyName : new ArrayList<>(propertyNames)) {
                IPropertyInfo currProp = propertyInfoProvider.getProperty(type, currPropertyName);
                if (currProp.isWritable()) {
                    continue;
                }
                // Is is just an evaluating property which has to be re-evaluated because of the change on
                // the current property
                PropertyChangeMixin.evaluateDependentProperties(type, currProp, propertyNames, propertyInfoProvider);
            }
            if (startCount == propertyNames.size()) {
                break;
            }
        }
        String[] normalPropertyNames = propertyNames.toArray(String[]::new);
        propertyNames.clear();
        for (String normalPropertyName : normalPropertyNames) {
            propertyNames.add(Introspector.decapitalize(normalPropertyName));
        }
        this.propertyNames = propertyNames.toArray(String[]::new);
        boolean firesToBeCreatedPCE = false;
        unknownValues = PropertyChangeMixin.createArrayOfValues(PropertyChangeMixin.UNKNOWN_VALUE, this.propertyNames.length);
        for (String invokedPropertyName : this.propertyNames) {
            firesToBeCreatedPCE |= IDataObject.P_TO_BE_CREATED.equals(invokedPropertyName);
        }
        this.firesToBeCreatedPCE = firesToBeCreatedPCE;
        if (prop.isReadable()) {
            getDelegate = new PropertyInfoItem(prop);
        } else {
            getDelegate = null;
        }
    }

    private <A extends Annotation> A findAnnotation(Class<?> type, Class<A> annotationType, boolean checkEmbedded) {
        Class<?> currType = type;
        while (currType != null) {
            A propertyChangeAspect = currType.getAnnotation(annotationType);
            if (propertyChangeAspect != null) {
                return propertyChangeAspect;
            }
            for (Class<?> interfaceType : currType.getInterfaces()) {
                propertyChangeAspect = interfaceType.getAnnotation(annotationType);
                if (propertyChangeAspect != null) {
                    return propertyChangeAspect;
                }
            }
            currType = currType.getSuperclass();
        }
        if (!checkEmbedded) {
            return null;
        }
        if (!IEmbeddedType.class.isAssignableFrom(type)) {
            return null;
        }
        for (Field declaredField : ReflectUtil.getDeclaredFieldInHierarchy(type, parentFieldName)) {
            return findAnnotation(declaredField.getType(), annotationType, true);
        }
        return null;
    }
}
