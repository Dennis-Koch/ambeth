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
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.annotation.Interning;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.typeinfo.Transient;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class IntermediatePrimitiveMember extends PrimitiveMember implements IPrimitiveMemberWrite {
    protected final String propertyName;

    @Getter
    protected final Class<?> entityType;

    @Getter
    protected final Class<?> declaringType;

    @Getter
    protected final Class<?> realType;

    @Getter
    protected final Class<?> elementType;

    @Getter
    protected final Annotation[] annotations;

    protected final HashMap<Class<?>, Annotation> annotationMap;

    @Getter
    @Setter
    protected boolean technicalMember;

    @Getter
    @Setter
    protected IInterningFeature interningProcedure;

    @Getter
    @Setter
    protected boolean isTransient;

    @Getter
    @Setter
    protected PrimitiveMember definedBy;

    public IntermediatePrimitiveMember(Class<?> declaringType, Class<?> entityType, Class<?> realType, Class<?> elementType, String propertyName, Annotation[] annotations,
            IInterningFeature interningProcedure) {
        this.declaringType = declaringType;
        this.entityType = entityType;
        this.realType = realType;
        this.elementType = elementType;
        this.propertyName = propertyName;
        this.interningProcedure = interningProcedure;
        if (annotations != null) {
            annotationMap = new HashMap<>();
            for (var annotation : annotations) {
                annotationMap.put(annotation.annotationType(), annotation);
            }
            this.annotations = annotations;

            if (getAnnotation(Transient.class) != null) {
                this.isTransient = true;
            }
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

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Annotation> V getAnnotation(Class<V> annotationType) {
        return (V) annotationMap.get(annotationType);
    }

    protected RuntimeException createException() {
        return new UnsupportedOperationException("This in an intermediate member which works only as a stub for a later bytecode-enhanced member");
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
