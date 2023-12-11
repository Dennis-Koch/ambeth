package com.koch.ambeth.service.metadata;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.typeinfo.AbstractPropertyInfo;
import com.koch.ambeth.util.collections.HashMap;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class IntermediatePrimitiveMember extends PrimitiveMember implements IPrimitiveMemberWrite {
    protected final String propertyName;

    protected final Class<?> entityType;

    protected final Class<?> declaringType;

    protected final Class<?> realType;

    protected final Class<?> elementType;

    protected final Annotation[] annotations;

    protected final HashMap<Class<?>, Annotation> annotationMap;

    protected boolean technicalMember;

    protected boolean isTransient;

    protected PrimitiveMember definedBy;

    public IntermediatePrimitiveMember(Class<?> declaringType, Class<?> entityType, Class<?> realType, Class<?> elementType, String propertyName, Annotation[] annotations) {
        this.declaringType = declaringType;
        this.entityType = entityType;
        this.realType = realType;
        this.elementType = elementType;
        this.propertyName = propertyName;
        if (annotations != null) {
            annotationMap = new HashMap<>();
            for (var annotation : annotations) {
                annotationMap.put(annotation.annotationType(), annotation);
            }
            this.annotations = annotations;
        } else {
            annotationMap = null;
            this.annotations = AbstractPropertyInfo.EMPTY_ANNOTATIONS;
        }
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    @Override
    public Class<?> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Class<?> getRealType() {
        return realType;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Annotation> V getAnnotation(Class<V> annotationType) {
        return (V) annotationMap.get(annotationType);
    }

    protected RuntimeException createException() {
        return new UnsupportedOperationException("This in an intermediate member which works only as a stub for a later bytecode-enhanced member");
    }

    @Override
    public boolean isTechnicalMember() {
        return technicalMember;
    }

    @Override
    public void setTechnicalMember(boolean technicalMember) {
        this.technicalMember = technicalMember;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    @Override
    public PrimitiveMember getDefinedBy() {
        return definedBy;
    }

    @Override
    public void setDefinedBy(PrimitiveMember definedBy) {
        this.definedBy = definedBy;
    }

    @Override
    public Object getNullEquivalentValue() {
        throw createException();
    }

    @Override
    public boolean isToMany() {
        return Collection.class.isAssignableFrom(getRealType());
    }

    @Override
    public Class<?> getElementType() {
        return elementType;
    }

    @Override
    public Class<?> getEntityType() {
        return entityType;
    }

    @Override
    public Object getValue(Object obj) {
        throw createException();
    }

    @Override
    public Object getValue(Object obj, boolean allowNullEquivalentValue) {
        throw createException();
    }

    @Override
    public void setValue(Object obj, Object value) {
        throw createException();
    }
}
