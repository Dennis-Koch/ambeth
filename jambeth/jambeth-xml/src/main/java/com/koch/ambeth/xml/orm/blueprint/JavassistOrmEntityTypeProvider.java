package com.koch.ambeth.xml.orm.blueprint;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.IOrmEntityTypeProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintProvider;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationBlueprint;
import com.koch.ambeth.util.IClassByteContentRegistry;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.WeakHashMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.SneakyThrows;

import java.beans.Introspector;
import java.util.Collection;

public class JavassistOrmEntityTypeProvider implements IOrmEntityTypeProvider, IStartingBean {
    @Autowired(optional = true)
    protected IBlueprintProvider blueprintProvider;

    @Autowired
    protected IClassLoaderProvider classLoaderProvider;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IThreadLocalObjectCollector objectCollector;

    protected ClassPool pool;
    protected CtClass stringClass;
    protected WeakHashMap<String, Class<?>> alreadyLoadedClasses = new WeakHashMap<>();

    @LogInstance
    private ILogger log;

    @SneakyThrows
    @Override
    public Class<?> resolveEntityType(String entityTypeName) {
        if (blueprintProvider == null) {
            throw new IllegalStateException("No " + IBlueprintProvider.class.getName() + " injected. This is an illegal state");
        }

        if (alreadyLoadedClasses.containsKey(entityTypeName)) {
            return alreadyLoadedClasses.get(entityTypeName);
        }
        var entityTypeBlueprint = blueprintProvider.resolveEntityTypeBlueprint(entityTypeName);

        CtClass newClass;
        if (entityTypeBlueprint.getIsClass()) {
            newClass = pool.makeClass(entityTypeName);
        } else {
            newClass = pool.makeInterface(entityTypeName);
        }

        if (entityTypeBlueprint.getInterfaces() != null) {
            for (String aClass : entityTypeBlueprint.getInterfaces()) {
                newClass.addInterface(pool.get(aClass));
            }
        }

        if (entityTypeBlueprint.getSuperclass() != null) {
            if (entityTypeBlueprint.getIsClass()) {
                newClass.setSuperclass(pool.get(entityTypeBlueprint.getSuperclass()));
            } else {
                throw new IllegalArgumentException(entityTypeBlueprint.getName() + " is an interface but has a superclass.");
            }
        }

        var constPool = newClass.getClassFile().getConstPool();
        if (entityTypeBlueprint.getAnnotations() != null) {
            var interfAnnotationAttributeInfo = createAnnotationAttribute(entityTypeBlueprint.getAnnotations(), constPool);
            newClass.getClassFile().addAttribute(interfAnnotationAttributeInfo);
        }

        if (entityTypeBlueprint.getProperties() != null) {
            for (var prop : entityTypeBlueprint.getProperties()) {
                if (entityTypeBlueprint.getIsClass()) {
                    var ctField = new CtField(pool.get(prop.getType()), Introspector.decapitalize(prop.getName()), newClass);
                    newClass.addField(ctField);
                    newClass.addMethod(CtNewMethod.getter("get" + prop.getName(), ctField));
                    newClass.addMethod(CtNewMethod.setter("set" + prop.getName(), ctField));
                } else {
                    var resultType = pool.get(prop.getType());
                    var ctGetMethod = new CtMethod(resultType, "get" + prop.getName(), null, newClass);
                    newClass.addMethod(ctGetMethod);
                    var annotationAttributeInfo = createAnnotationAttribute(prop.getAnnotations(), constPool);
                    ctGetMethod.getMethodInfo().addAttribute(annotationAttributeInfo);
                    if (!prop.isReadonly()) {
                        var parameters = new CtClass[] { resultType };
                        var ctSetMethod = new CtMethod(CtClass.voidType, "set" + prop.getName(), parameters, newClass);
                        newClass.addMethod(ctSetMethod);
                    }
                }
            }
        }
        var classLoader = classLoaderProvider.getClassLoader();
        var entityType = newClass.toClass(classLoader, null);

        if (classLoader instanceof IClassByteContentRegistry classByteContentRegistry) {
            classByteContentRegistry.registerContent(entityType, newClass.toBytecode());
        }
        alreadyLoadedClasses.put(entityTypeName, entityType);
        return entityType;
    }

    protected AnnotationsAttribute createAnnotationAttribute(Collection<? extends IEntityAnnotationBlueprint> annotations, ConstPool constPool) throws Exception {
        var attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        for (var anno : annotations) {
            var annotation = new Annotation(anno.getType(), constPool);
            var annotationType = pool.get(anno.getType());

            for (var annotatedProp : anno.getProperties()) {
                var annotatedPropMethod = annotationType.getDeclaredMethod(annotatedProp.getName());

                if (annotatedPropMethod.getReturnType().equals(CtClass.booleanType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new BooleanMemberValue(conversionHelper.convertValueToType(Boolean.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.byteType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new ByteMemberValue(conversionHelper.convertValueToType(Byte.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.charType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new CharMemberValue(conversionHelper.convertValueToType(Character.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.doubleType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new DoubleMemberValue(conversionHelper.convertValueToType(Double.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.floatType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new FloatMemberValue(conversionHelper.convertValueToType(Float.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.intType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new IntegerMemberValue(conversionHelper.convertValueToType(Integer.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.longType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new LongMemberValue(conversionHelper.convertValueToType(Long.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(CtClass.shortType)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new ShortMemberValue(conversionHelper.convertValueToType(Short.class, annotatedProp.getValue()), constPool));
                } else if (annotatedPropMethod.getReturnType().equals(stringClass)) {
                    annotation.addMemberValue(annotatedProp.getName(), //
                            new StringMemberValue(conversionHelper.convertValueToType(String.class, annotatedProp.getValue()), constPool));
                }
            }
            attr.addAnnotation(annotation);
        }
        return attr;
    }

    @Override
    public void afterStarted() throws Throwable {
        pool = ClassPool.getDefault();
        stringClass = pool.get(String.class.getName());
    }

}
