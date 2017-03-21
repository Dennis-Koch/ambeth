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

import java.beans.Introspector;
import java.util.Collection;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.IOrmEntityTypeProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintProvider;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationPropertyBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityPropertyBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.WeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
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

public class JavassistOrmEntityTypeProvider implements IOrmEntityTypeProvider, IStartingBean {
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintProvider blueprintProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected ClassPool pool;

	protected CtClass stringClass;

	protected WeakHashMap<String, Class<?>> alreadLoadedClasses = new WeakHashMap<>();

	@Override
	public Class<?> resolveEntityType(String entityTypeName) {
		if (blueprintProvider == null) {
			throw new IllegalStateException(
					"No " + IBlueprintProvider.class.getName() + " injected. This is an illegal state");
		}

		if (alreadLoadedClasses.containsKey(entityTypeName)) {
			return alreadLoadedClasses.get(entityTypeName);
		}
		IEntityTypeBlueprint entityTypeBlueprint =
				blueprintProvider.resolveEntityTypeBlueprint(entityTypeName);

		CtClass newClass;
		if (entityTypeBlueprint.getIsClass()) {
			newClass = pool.makeClass(entityTypeName);
		}
		else {
			newClass = pool.makeInterface(entityTypeName);
		}
		try {
			if (entityTypeBlueprint.getInterfaces() != null) {
				for (String aClass : entityTypeBlueprint.getInterfaces()) {
					newClass.addInterface(pool.get(aClass));
				}
			}

			if (entityTypeBlueprint.getSuperclass() != null) {
				if (entityTypeBlueprint.getIsClass()) {
					newClass.setSuperclass(pool.get(entityTypeBlueprint.getSuperclass()));
				}
				else {
					throw new IllegalArgumentException(
							entityTypeBlueprint.getName() + " is an interface but has a superclass.");
				}
			}

			ConstPool constPool = newClass.getClassFile().getConstPool();
			if (entityTypeBlueprint.getAnnotations() != null) {
				AnnotationsAttribute interfAnnotationAttributeInfo =
						createAnnotationAttribute(entityTypeBlueprint.getAnnotations(), constPool);
				newClass.getClassFile().addAttribute(interfAnnotationAttributeInfo);
			}

			if (entityTypeBlueprint.getProperties() != null) {
				for (IEntityPropertyBlueprint prop : entityTypeBlueprint.getProperties()) {
					if (entityTypeBlueprint.getIsClass()) {
						CtField ctField = new CtField(pool.get(prop.getType()),
								Introspector.decapitalize(prop.getName()), newClass);
						newClass.addField(ctField);
						newClass.addMethod(CtNewMethod.getter("get" + prop.getName(), ctField));
						newClass.addMethod(CtNewMethod.setter("set" + prop.getName(), ctField));
					}
					else {
						CtClass resultType = pool.get(prop.getType());
						CtMethod ctGetMethod = new CtMethod(resultType, "get" + prop.getName(), null, newClass);
						newClass.addMethod(ctGetMethod);
						AnnotationsAttribute annotationAttributeInfo =
								createAnnotationAttribute(prop.getAnnotations(), constPool);
						ctGetMethod.getMethodInfo().addAttribute(annotationAttributeInfo);
						if (!prop.isReadonly()) {
							CtClass[] parameters = new CtClass[] {resultType};
							CtMethod ctSetMethod =
									new CtMethod(CtClass.voidType, "set" + prop.getName(), parameters, newClass);
							newClass.addMethod(ctSetMethod);

						}
					}
				}
			}

			Class<?> entityType;

			entityType = newClass.toClass();

			alreadLoadedClasses.put(entityTypeName, entityType);
			return entityType;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected AnnotationsAttribute createAnnotationAttribute(
			Collection<? extends IEntityAnnotationBlueprint> annotations, ConstPool constPool)
			throws Exception {
		AnnotationsAttribute attr =
				new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		for (IEntityAnnotationBlueprint anno : annotations) {
			Annotation annot = new Annotation(anno.getType(), constPool);
			CtClass annotClass = pool.get(anno.getType());

			for (IEntityAnnotationPropertyBlueprint annonProp : anno.getProperties()) {
				CtMethod annonPropMethod = annotClass.getDeclaredMethod(annonProp.getName());

				if (annonPropMethod.getReturnType().equals(CtClass.booleanType)) {
					annot.addMemberValue(annonProp.getName(), //
							new BooleanMemberValue(
									conversionHelper.convertValueToType(Boolean.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.byteType)) {
					annot.addMemberValue(annonProp.getName(), //
							new ByteMemberValue(
									conversionHelper.convertValueToType(Byte.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.charType)) {
					annot.addMemberValue(annonProp.getName(), //
							new CharMemberValue(
									conversionHelper.convertValueToType(Character.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.doubleType)) {
					annot.addMemberValue(annonProp.getName(), //
							new DoubleMemberValue(
									conversionHelper.convertValueToType(Double.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.floatType)) {
					annot.addMemberValue(annonProp.getName(), //
							new FloatMemberValue(
									conversionHelper.convertValueToType(Float.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.intType)) {
					annot.addMemberValue(annonProp.getName(), //
							new IntegerMemberValue(
									conversionHelper.convertValueToType(Integer.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.longType)) {
					annot.addMemberValue(annonProp.getName(), //
							new LongMemberValue(
									conversionHelper.convertValueToType(Long.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(CtClass.shortType)) {
					annot.addMemberValue(annonProp.getName(), //
							new ShortMemberValue(
									conversionHelper.convertValueToType(Short.class, annonProp.getValue()),
									constPool));
				}
				else if (annonPropMethod.getReturnType().equals(stringClass)) {
					annot.addMemberValue(annonProp.getName(), //
							new StringMemberValue(
									conversionHelper.convertValueToType(String.class, annonProp.getValue()),
									constPool));
				}
			}
			attr.addAnnotation(annot);
		}
		return attr;
	}

	@Override
	public void afterStarted() throws Throwable {
		pool = ClassPool.getDefault();
		stringClass = pool.get(String.class.getName());
	}

}
