package com.koch.ambeth.merge.bytecode.visitor;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.ioc.typeinfo.AbstractPropertyInfo;
import com.koch.ambeth.ioc.typeinfo.FieldPropertyInfo;
import com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class EntityMetaDataMemberVisitor extends ClassGenerator {
    public static final MethodInstance template_m_getValue = new MethodInstance(null, Member.class, Object.class, "getValue", Object.class);
    public static final MethodInstance template_m_getValueWithFlag = new MethodInstance(null, Member.class, Object.class, "getValue", Object.class, boolean.class);
    protected static final MethodInstance template_m_canRead = new MethodInstance(null, Member.class, boolean.class, "canRead");
    protected static final MethodInstance template_m_canWrite = new MethodInstance(null, Member.class, boolean.class, "canWrite");
    protected static final MethodInstance template_m_getAnnotation = new MethodInstance(null, Member.class, Annotation.class, "getAnnotation", Class.class);

    protected static final MethodInstance template_m_getAnnotations = new MethodInstance(null, Member.class, Annotation[].class, "getAnnotations");
    protected static final MethodInstance template_m_getDeclaringType = new MethodInstance(null, Member.class, Class.class, "getDeclaringType");
    protected static final MethodInstance template_m_getNullEquivalentValue = new MethodInstance(null, Member.class, Object.class, "getNullEquivalentValue");
    protected static final MethodInstance template_m_getName = new MethodInstance(null, Member.class, String.class, "getName");
    protected static final MethodInstance template_m_getEntityType = new MethodInstance(null, Member.class, Class.class, "getEntityType");
    protected static final MethodInstance template_m_getElementType = new MethodInstance(null, Member.class, Class.class, "getElementType");
    protected static final MethodInstance template_m_getRealType = new MethodInstance(null, Member.class, Class.class, "getRealType");
    protected static final MethodInstance template_m_isToMany = new MethodInstance(null, Member.class, boolean.class, "isToMany");
    protected static final MethodInstance template_m_setValue = new MethodInstance(null, Member.class, void.class, "setValue", Object.class, Object.class);

    protected final Class<?> declaringType;

    protected final Class<?> entityType;

    protected final String memberName;

    protected final Class<?> forcedElementType;

    protected final IEntityMetaDataProvider entityMetaDataProvider;

    protected final IPropertyInfo[] propertyPath;

    public EntityMetaDataMemberVisitor(ClassVisitor cv, Class<?> declaringType, Class<?> entityType, String memberName, Class<?> forcedElementType, IEntityMetaDataProvider entityMetaDataProvider,
            IPropertyInfo[] propertyPath) {
        super(cv);
        this.declaringType = declaringType;
        this.entityType = entityType;
        this.memberName = memberName;
        this.forcedElementType = forcedElementType;
        this.entityMetaDataProvider = entityMetaDataProvider;
        this.propertyPath = propertyPath;
    }

    @Override
    public void visitEnd() {
        implementCanRead(propertyPath);
        implementCanWrite(propertyPath);
        implementGetAnnotation(propertyPath);
        implementGetDeclaringType(propertyPath);
        implementGetName(propertyPath);
        implementGetNullEquivalentValue(propertyPath);
        implementGetEntityType(propertyPath);
        implementGetElementType(propertyPath);
        implementGetRealType(propertyPath);
        implementIsToMany(propertyPath);
        implementGetValue(propertyPath);
        implementSetValue(propertyPath);
        super.visitEnd();
    }

    protected void implementCanRead(IPropertyInfo[] property) {
        var mv = visitMethod(template_m_canRead);
        mv.push(property[property.length - 1].isReadable());
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementCanWrite(IPropertyInfo[] property) {
        var mv = visitMethod(template_m_canWrite);
        mv.push(property[property.length - 1].isWritable());
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetNullEquivalentValue(IPropertyInfo[] property) {
        var mv = visitMethod(template_m_getNullEquivalentValue);
        var propertyType = property[property.length - 1].getPropertyType();
        mv.pushNullOrZero(propertyType);
        if (propertyType.isPrimitive()) {
            mv.box(Type.getType(propertyType));
        }
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetEntityType(IPropertyInfo[] property) {
        var mv = visitMethod(template_m_getEntityType);
        var entityType = this.entityType;
        var metaData = entityMetaDataProvider.getMetaData(entityType, true);
        if (metaData != null) {
            entityType = metaData.getEntityType();
        }
        mv.push(entityType);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetElementType(IPropertyInfo[] property) {
        var elementType = forcedElementType != null ? forcedElementType : property[property.length - 1].getElementType();
        var mv = visitMethod(template_m_getElementType);
        mv.push(elementType);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetRealType(IPropertyInfo[] property) {
        var realType = property[property.length - 1].getPropertyType();
        if (forcedElementType != null && realType == property[property.length - 1].getElementType()) {
            realType = forcedElementType;
        }
        var mv = visitMethod(template_m_getRealType);
        mv.push(realType);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetAnnotation(IPropertyInfo[] property) {
        var annotations = property[property.length - 1].getAnnotations();
        Annotation[] annotationsArray;
        if (annotations.length == 0) {
            var mv = visitMethod(template_m_getAnnotation);
            mv.pushNull();
            mv.returnValue();
            mv.endMethod();
            annotationsArray = AbstractPropertyInfo.EMPTY_ANNOTATIONS;
        } else {
            var typeToAnnotationMap = HashMap.<Class<?>, Annotation>create(annotations.length, 0.5f);
            for (var annotation : annotations) {
                typeToAnnotationMap.put(annotation.annotationType(), annotation);
            }
            var f_typeToAnnotationMap = implementStaticAssignedField("sf__typeToAnnotationMap", typeToAnnotationMap);
            var mv = visitMethod(template_m_getAnnotation);
            mv.getThisField(f_typeToAnnotationMap);
            mv.loadArg(0);
            mv.invokeVirtual(new MethodInstance(null, HashMap.class, Object.class, "get", Object.class));
            mv.checkCast(Annotation.class);
            mv.returnValue();
            mv.endMethod();
            annotationsArray = annotations.clone();
        }
        var f_annotationsArray = implementStaticAssignedField("sf__annotations", annotationsArray);
        var mv = visitMethod(template_m_getAnnotations);
        mv.getThisField(f_annotationsArray);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetDeclaringType(IPropertyInfo[] property) {
        var mv = visitMethod(template_m_getDeclaringType);
        mv.push(declaringType);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetName(IPropertyInfo[] property) {
        var compositeName = new StringBuilder();
        for (int a = 0, size = property.length; a < size; a++) {
            if (a > 0) {
                compositeName.append('.');
            }
            compositeName.append(property[a].getName());
        }
        var mv = visitMethod(template_m_getName);
        mv.push(compositeName.toString());
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetValue(IPropertyInfo[] propertyPath) {
        for (int a = 0, size = propertyPath.length; a < size; a++) {
            var property = propertyPath[a];
            if (property instanceof MethodPropertyInfo && ((MethodPropertyInfo) property).getGetter() == null) {
                throw new IllegalStateException("Property not readable: " + property.getEntityType().getName() + "." + property.getName());
            }
        }
        implementGetValueIntern(template_m_getValue, propertyPath);
        implementGetValueIntern(template_m_getValueWithFlag, propertyPath);
    }

    protected void implementGetValueIntern(MethodInstance mi, IPropertyInfo[] propertyPath) {
        var mv = visitMethod(mi);

        var l_finish = mv.newLabel();
        mv.loadArg(0);
        var typeOfArgumentOnStack = Type.getType(Object.class);
        for (int a = 0, size = propertyPath.length - 1; a < size; a++) {
            typeOfArgumentOnStack = invokeGetProperty(mv, propertyPath[a], typeOfArgumentOnStack);
            mv.dup();
            mv.ifNull(l_finish);
        }
        var lastProperty = propertyPath[propertyPath.length - 1];
        typeOfArgumentOnStack = invokeGetProperty(mv, lastProperty, typeOfArgumentOnStack);
        if (lastProperty.getPropertyType().isPrimitive()) {
            var pType = Type.getType(lastProperty.getPropertyType());
            var loc_value = mv.newLocal(pType);
            mv.storeLocal(loc_value);
            mv.loadLocal(loc_value);
            var l_valueIsNonZero = mv.newLabel();

            mv.ifZCmp(pType, GeneratorAdapter.NE, l_valueIsNonZero);

            if (mv.getMethod().getParameters().length == 2) {
                var l_nullAllowed = mv.newLabel();
                // check null-equi flag
                mv.loadArg(1);
                mv.ifZCmp(GeneratorAdapter.EQ, l_nullAllowed);
                mv.pushNullOrZero(pType);
                mv.box(pType);
                mv.returnValue();
                mv.mark(l_nullAllowed);
            }
            mv.pushNullOrZero(mv.getMethod().getReturnType());
            mv.returnValue();

            mv.mark(l_valueIsNonZero);
            mv.loadLocal(loc_value);
            mv.valueOf(pType);
        }
        mv.mark(l_finish);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementSetValue(IPropertyInfo[] propertyPath) {
        var mv = visitMethod(template_m_setValue);

        var lastProperty = propertyPath[propertyPath.length - 1];
        if (lastProperty instanceof MethodPropertyInfo && ((MethodPropertyInfo) lastProperty).getSetter() == null) {
            mv.throwException(Type.getType(UnsupportedOperationException.class), "Property not writable: " + lastProperty.getEntityType().getName() + "." + lastProperty.getName());
            mv.returnValue();
            mv.endMethod();
            return;
        }
        mv.loadArg(0);
        var typeOfArgumentOnStack = Type.getType(Object.class);

        for (int a = 0, size = propertyPath.length - 1; a < size; a++) {
            typeOfArgumentOnStack = invokeGetProperty(mv, propertyPath[a], typeOfArgumentOnStack);
        }
        if (!lastProperty.getDeclaringType().equals(typeOfArgumentOnStack)) {
            mv.checkCast(lastProperty.getDeclaringType());
        }
        mv.loadArg(1);
        var lastPropertyType = Type.getType(lastProperty.getPropertyType());
        if (lastProperty.getPropertyType().isPrimitive()) {
            var pType = Type.getType(lastProperty.getPropertyType());
            var l_valueIsNonNull = mv.newLabel();
            var l_valueIsValid = mv.newLabel();

            mv.ifNonNull(l_valueIsNonNull);
            mv.pushNullOrZero(pType);
            mv.goTo(l_valueIsValid);

            mv.mark(l_valueIsNonNull);
            mv.loadArg(1);
            mv.unbox(pType);
            mv.mark(l_valueIsValid);
        } else {
            mv.checkCast(lastPropertyType);
        }
        invokeSetProperty(mv, lastProperty);
        mv.returnValue();

        mv.endMethod();
    }

    protected Type invokeGetProperty(MethodGenerator mv, IPropertyInfo property, Type typeOfArgumentOnStack) {
        if (property instanceof MethodPropertyInfo) {
            var method = ((MethodPropertyInfo) property).getGetter();
            var mi = new MethodInstance(method);
            if (method.getDeclaringClass().isInterface()) {
                mv.invokeInterface(mi);
            } else {
                if (!mi.getOwner().equals(typeOfArgumentOnStack)) {
                    mv.checkCast(mi.getOwner());
                }
                mv.invokeVirtual(mi);
                typeOfArgumentOnStack = mi.getReturnType();
            }
        } else {
            var field = ((FieldPropertyInfo) property).getBackingField();
            var fi = new FieldInstance(field);
            if (!fi.getOwner().equals(typeOfArgumentOnStack)) {
                mv.checkCast(fi.getOwner());
            }
            mv.getField(fi);
            typeOfArgumentOnStack = fi.getType();
        }
        return typeOfArgumentOnStack;
    }

    protected void invokeSetProperty(MethodGenerator mv, IPropertyInfo property) {
        if (property instanceof MethodPropertyInfo) {
            var method = ((MethodPropertyInfo) property).getSetter();
            if (method.getDeclaringClass().isInterface()) {
                mv.invokeInterface(new MethodInstance(method));
            } else {
                mv.invokeVirtual(new MethodInstance(method));
            }
        } else {
            var field = ((FieldPropertyInfo) property).getBackingField();
            mv.putField(new FieldInstance(field));
        }
    }

    protected void implementIsToMany(IPropertyInfo[] propertyPath) {
        var mv = visitMethod(template_m_isToMany);
        mv.push(Collection.class.isAssignableFrom(propertyPath[propertyPath.length - 1].getPropertyType()));
        mv.returnValue();
        mv.endMethod();
    }

}
