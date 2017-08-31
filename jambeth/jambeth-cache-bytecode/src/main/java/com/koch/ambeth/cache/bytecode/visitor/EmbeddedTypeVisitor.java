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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.IOverrideConstructorDelegate;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.cache.mixin.EmbeddedTypeMixin;
import com.koch.ambeth.cache.mixin.PropertyChangeMixin.PropertyEntry;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.util.model.IEmbeddedType;

public class EmbeddedTypeVisitor extends ClassGenerator {
	public static final Class<?> templateType = EmbeddedTypeMixin.class;

	public static final String templatePropertyName = "__" + templateType.getSimpleName();

	public static final PropertyInstance t_p_Parent =
			PropertyInstance.findByTemplate(IEmbeddedType.class, PropertyEntry.parentPropertyName,
					Object.class, false);

	public static final PropertyInstance t_p_Root =
			PropertyInstance.findByTemplate(IEmbeddedType.class, "Root", Object.class, false);

	public static final MethodInstance m_getRoot =
			new MethodInstance(null, templateType, Object.class, "getRoot", IEmbeddedType.class);

	public static FieldInstance getParentEntityField(ClassGenerator cv) {
		FieldInstance f_parentEntity =
				getState().getAlreadyImplementedField(PropertyEntry.parentFieldName);
		if (f_parentEntity != null) {
			return f_parentEntity;
		}
		Class<?> parentEntityType =
				EmbeddedEnhancementHint.getParentObjectType(getState().getContext());
		f_parentEntity =
				new FieldInstance(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, PropertyEntry.parentFieldName,
						null, parentEntityType);
		return cv.implementField(f_parentEntity);
	}

	public static PropertyInstance getParentEntityProperty(ClassGenerator cv) {
		PropertyInstance p_parentEntity = PropertyInstance.findByTemplate(t_p_Parent, true);
		if (p_parentEntity != null) {
			return p_parentEntity;
		}
		FieldInstance f_parentField = getParentEntityField(cv);
		p_parentEntity = cv.implementGetter(t_p_Parent, f_parentField);

		if (p_parentEntity == null) {
			throw new IllegalStateException("Must never happen");
		}
		return p_parentEntity;
	}

	public static PropertyInstance getEmbeddedTypeTemplateProperty(ClassGenerator cv) {
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance p_embeddedTypeTemplate =
				PropertyInstance.findByTemplate(templatePropertyName, bean.getClass(), true);
		if (p_embeddedTypeTemplate != null) {
			return p_embeddedTypeTemplate;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	public static PropertyInstance getRootEntityProperty(ClassGenerator cv) {
		PropertyInstance p_root = PropertyInstance.findByTemplate(t_p_Root, true);
		if (p_root != null) {
			return p_root;
		}
		final PropertyInstance p_embeddedTypeTemplate = getEmbeddedTypeTemplateProperty(cv);
		p_root = cv.implementProperty(t_p_Root, new Script() {
			@Override
			public void execute(MethodGenerator mg) {
				mg.callThisGetter(p_embeddedTypeTemplate);
				mg.loadThis();
				mg.invokeVirtual(m_getRoot);
				mg.returnValue();
			}
		}, null);

		if (p_root == null) {
			throw new IllegalStateException("Must never happen");
		}
		return p_root;
	}

	public EmbeddedTypeVisitor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visitEnd() {
		// force implementation
		FieldInstance f_parentEntity = getParentEntityField(this);
		getParentEntityProperty(this);
		getRootEntityProperty(this);
		implementEmbeddedConstructor(f_parentEntity);
		super.visitEnd();
	}

	protected void implementEmbeddedConstructor(final FieldInstance f_parentEntity) {
		overrideConstructors(new IOverrideConstructorDelegate() {
			@Override
			public void invoke(ClassGenerator cv, ConstructorInstance superConstructor) {
				implementEmbeddedConstructor(f_parentEntity, superConstructor);
			}
		});
	}

	protected void implementEmbeddedConstructor(FieldInstance f_parentEntity,
			ConstructorInstance superConstructor) {
		if (superConstructor.getParameters().length > 0
				&& superConstructor.getParameters()[0].equals(f_parentEntity.getType())) {
			// super constructor already enhanced
			return;
		}
		boolean baseIsEnhanced = false;// EntityEnhancer.IsEnhancedType(vs.CurrentType);
		Type[] parameters = superConstructor.getParameters();
		Type[] types;
		if (baseIsEnhanced) {
			// Only Pass-through constructors necessary. So signature remains the same
			types = null;// TypeUtil.GetClassesToTypes(..GetTypes(parameters);
		}
		else {
			types = new Type[parameters.length + 1];
			for (int a = parameters.length + 1; a-- > 1;) {
				types[a] = parameters[a - 1];
			}
			types[0] = f_parentEntity.getType();
		}
		MethodGenerator mv = visitMethod(new ConstructorInstance(Opcodes.ACC_PUBLIC, null, types));
		mv.loadThis();
		for (int a = 1, size = types.length; a < size; a++) {
			mv.loadArg(a); // Load constructor argument one by one, starting with the 2nd constructor
											// argument
		}
		mv.invokeConstructor(superConstructor);
		mv.putThisField(f_parentEntity, new Script() {
			@Override
			public void execute(MethodGenerator mg) {
				mg.loadArg(0);
			}
		});
		mv.returnValue();
		mv.endMethod();
	}
}
