package com.koch.ambeth.cache.bytecode.visitor;

/*-
 * #%L
 * jambeth-cache-bytecode
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.expr.PropertyExpression;
import com.koch.ambeth.ioc.typeinfo.FieldPropertyInfo;
import com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class DefaultPropertiesMethodVisitor extends ClassGenerator {
	protected IPropertyInfo[] propertyInfos;

	protected IThreadLocalObjectCollector objectCollector;

	public DefaultPropertiesMethodVisitor(ClassVisitor cv, IPropertyInfo[] propertyInfos,
			IThreadLocalObjectCollector objectCollector) {
		super(cv);
		this.propertyInfos = propertyInfos;
		this.objectCollector = objectCollector;
	}

	protected ISet<Class<?>> buildAllInterfaces(Class<?> currType) {
		ISet<Class<?>> interfaceSet = new HashSet<>();
		if (currType.isInterface()) {
			interfaceSet.add(currType);
		}
		while (currType != null) {
			Class<?>[] interfaces = currType.getInterfaces();
			for (int a = interfaces.length; a-- > 0;) {
				interfaceSet.add(interfaces[a]);
			}
			currType = currType.getSuperclass();
		}
		return interfaceSet;
	}

	@Override
	public void visitEnd() {
		HashMap<String, List<Method>> nameToMethodsMap = new HashMap<>();
		for (Method method : ReflectUtil.getMethods(getState().getOriginalType())) {
			List<Method> methodList = nameToMethodsMap.get(method.getName());
			if (methodList == null) {
				methodList = new ArrayList<>();
				nameToMethodsMap.put(method.getName(), methodList);
			}
			methodList.add(method);
		}
		ISet<Class<?>> allInterfaces = buildAllInterfaces(getState().getOriginalType());
		allInterfaces.addAll(buildAllInterfaces(getState().getCurrentType()));

		for (IPropertyInfo propertyInfo : propertyInfos) {
			if (propertyInfo instanceof FieldPropertyInfo) {
				// if there is neither a setter nor a getter we let the field untouched
				continue;
			}
			Method getter = ((MethodPropertyInfo) propertyInfo).getGetter();
			if (getter == null) {
				// look for abstract definition of the getter
				getter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(),
						propertyInfo.getPropertyType(), "get" + propertyInfo.getName());
			}
			if (getter != null && getter.isAnnotationPresent(PropertyExpression.class)) {
				// this member will be handled by another visitor
				continue;
			}
			Method setter = ((MethodPropertyInfo) propertyInfo).getSetter();
			if (setter == null) {
				// look for abstract definition of the setter
				setter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(), void.class,
						"set" + propertyInfo.getName(), propertyInfo.getPropertyType());
			}
			Method fluentSetter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(),
					getState().getOriginalType(), "with" + propertyInfo.getName(),
					propertyInfo.getPropertyType());
			if (fluentSetter == null) {
				for (Class<?> interfaceType : allInterfaces) {
					fluentSetter = ReflectUtil.getDeclaredMethod(true, interfaceType,
							null, "with" + propertyInfo.getName(),
							propertyInfo.getPropertyType());
					if (fluentSetter != null) {
						break;
					}
				}
			}
			MethodInstance m_getterTemplate = getter != null ? new MethodInstance(getter) : null;
			MethodInstance m_setterTemplate = setter != null ? new MethodInstance(setter) : null;
			MethodInstance m_fluentSetterTemplate =
					fluentSetter != null ? new MethodInstance(fluentSetter) : null;
			MethodInstance m_getter = MethodInstance.findByTemplate(m_getterTemplate, true);
			MethodInstance m_setter = MethodInstance.findByTemplate(m_setterTemplate, true);
			MethodInstance m_fluentSetter = MethodInstance.findByTemplate(m_fluentSetterTemplate, true);

			if (m_getter != null && m_setter != null) {
				// ensure both accessors are public
				if ((m_getter.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
					MethodGenerator mv = visitMethod(m_getter.deriveAccess(Opcodes.ACC_PUBLIC));
					mv.loadThis();
					mv.loadArgs();
					mv.invokeSuper(m_getter);
					mv.returnValue();
					mv.endMethod();
				}
				if ((m_setter.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
					MethodGenerator mv = visitMethod(m_setter.deriveAccess(Opcodes.ACC_PUBLIC));
					mv.loadThis();
					mv.loadArgs();
					mv.invokeSuper(m_setter);
					mv.returnValue();
					mv.endMethod();
				}
				if (m_fluentSetter != null && (m_fluentSetter.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
					MethodGenerator mv = visitMethod(m_fluentSetter.deriveAccess(Opcodes.ACC_PUBLIC));
					mv.loadThis();
					mv.loadArgs();
					mv.invokeSuper(m_fluentSetter);
					mv.returnValue();
					mv.endMethod();
				}
				continue;
			}
			if (m_getter != null || m_setter != null || m_fluentSetter != null) {
				// at least one of the accessors is explicitly implemented
				continue;
			}
			final FieldInstance f_backingField = ensureBackingField(propertyInfo);
			if (f_backingField == null) {
				continue;
			}
			if (m_setterTemplate == null) {
				m_setterTemplate = new MethodInstance(null, Opcodes.ACC_PUBLIC,
						m_setterTemplate != null ? m_setterTemplate.getReturnType() : Type.VOID_TYPE,
						"set" + propertyInfo.getName(), null, f_backingField.getType());
			}
			// implement setter
			m_setterTemplate = implementSetter(m_setterTemplate, f_backingField);
			List<Method> allSettersWithSameName = nameToMethodsMap.get(m_setterTemplate.getName());
			final MethodInstance f_m_setterTemplate = m_setterTemplate;
			if (allSettersWithSameName != null) {
				for (Method setterWithSameName : allSettersWithSameName) {
					MethodInstance m_setterWithSameName =
							MethodInstance.findByTemplate(setterWithSameName, true);
					if (m_setterWithSameName != null) {
						// method is implemented, so nothing to do
						continue;
					}
					MethodGenerator mv = visitMethod(new MethodInstance(setterWithSameName));
					if (mv.getMethod().getParameters().length != 1) {
						// this visitor handles only "true" setters with exactly one argument
						continue;
					}
					mv.callThisSetter(m_setterTemplate, new Script() {
						@Override
						public void execute(MethodGenerator mg) {
							mg.loadArg(0);
							mg.checkCast(f_m_setterTemplate.getParameters()[0]);
						}
					});
					mv.returnVoidOrThis();
					mv.endMethod();
				}
			}
			if (m_fluentSetterTemplate != null) {
				MethodGenerator mv = visitMethod(m_fluentSetterTemplate);
				if (mv.getMethod().getParameters().length != 1) {
					// this visitor handles only "true" setters with exactly one argument
					continue;
				}
				mv.callThisSetter(m_setterTemplate, new Script() {
					@Override
					public void execute(MethodGenerator mg) {
						mg.loadArg(0);
					}
				});
				mv.popIfReturnValue(m_setterTemplate);
				mv.returnVoidOrThis();
				mv.endMethod();
			}
			if (m_getterTemplate == null) {
				m_getterTemplate = new MethodInstance(null, Opcodes.ACC_PUBLIC, f_backingField.getType(),
						"get" + propertyInfo.getName(), null);
			}
			// implement getter
			m_getterTemplate = implementGetter(m_getterTemplate, f_backingField);
			List<Method> allGettersWithSameName = nameToMethodsMap.get(m_getterTemplate.getName());
			if (allGettersWithSameName != null) {
				for (Method getterWithSameName : allGettersWithSameName) {
					MethodInstance m_getterWithSameName =
							MethodInstance.findByTemplate(getterWithSameName, true);
					if (m_getterWithSameName != null) {
						// method is implemented, so nothing to do
						continue;
					}
					MethodGenerator mv = visitMethod(new MethodInstance(getterWithSameName));
					mv.callThisGetter(m_getterTemplate);
					mv.returnValue();
					mv.endMethod();
				}
			}
		}
		super.visitEnd();
	}

	protected FieldInstance ensureBackingField(IPropertyInfo propertyInfo) {
		Field backingField = propertyInfo.getBackingField();
		FieldInstance f_backingField;
		if (backingField != null) {
			return new FieldInstance(backingField);
		}
		else if (propertyInfo.getDeclaringType().isInterface()
				|| (propertyInfo.getDeclaringType().getModifiers() & Modifier.ABSTRACT) != 0) {
			String fieldName =
					StringConversionHelper.lowerCaseFirst(objectCollector, propertyInfo.getName());
			f_backingField = BytecodeBehaviorState.getState().getAlreadyImplementedField(fieldName);

			if (f_backingField == null) {
				String fieldSignature;
				if (((MethodPropertyInfo) propertyInfo).getGetter() != null) {
					fieldSignature = FieldInstance
							.getSignatureFromReturnType(((MethodPropertyInfo) propertyInfo).getGetter());
				}
				else {
					fieldSignature = FieldInstance
							.getSignatureFromParameterType(((MethodPropertyInfo) propertyInfo).getSetter(), 0);
				}

				// add field
				f_backingField = new FieldInstance(Opcodes.ACC_PROTECTED,
						StringConversionHelper.lowerCaseFirst(objectCollector, propertyInfo.getName()),
						fieldSignature, Type.getType(propertyInfo.getPropertyType()));

				f_backingField = implementField(f_backingField);
			}
			return f_backingField;
		}
		return null;
	}
}
