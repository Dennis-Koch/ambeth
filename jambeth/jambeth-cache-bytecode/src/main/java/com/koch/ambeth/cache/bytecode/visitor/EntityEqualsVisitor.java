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
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.cache.mixin.EntityEqualsMixin;
import com.koch.ambeth.cache.proxy.IEntityEquals;
import com.koch.ambeth.util.IPrintable;

public class EntityEqualsVisitor extends ClassGenerator
{
	public static final Class<?> templateType = EntityEqualsMixin.class;

	public static final String templatePropertyName = "__" + templateType.getSimpleName();

	private static final MethodInstance entityEquals_Equals = new MethodInstance(null, templateType, boolean.class, "equals", IEntityEquals.class, Object.class);

	private static final MethodInstance entityEquals_HashCode = new MethodInstance(null, templateType, int.class, "hashCode", IEntityEquals.class);

	private static final MethodInstance entityEquals_toString_Obj = new MethodInstance(null, templateType, String.class, "toString", IEntityEquals.class,
			IPrintable.class);

	private static final MethodInstance entityEquals_toString_Printable = new MethodInstance(null, templateType, void.class, "toString", IEntityEquals.class,
			StringBuilder.class);

	public static PropertyInstance getEntityEqualsTemplateProperty(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance p_embeddedTypeTemplate = PropertyInstance.findByTemplate(templatePropertyName, bean.getClass(), true);
		if (p_embeddedTypeTemplate != null)
		{
			return p_embeddedTypeTemplate;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	public EntityEqualsVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		implementIEntityEqualsCode();
		super.visitEnd();
	}

	protected void implementIEntityEqualsCode()
	{
		PropertyInstance p_entityEqualsTemplate = getEntityEqualsTemplateProperty(this);
		implementEqualsMethod(p_entityEqualsTemplate);
		implementHashCodeMethod(p_entityEqualsTemplate);
		implementToStringMethod(p_entityEqualsTemplate);
	}

	protected void implementEqualsMethod(PropertyInstance p_entityEqualsTemplate)
	{
		MethodInstance methodTemplate = new MethodInstance(null, Object.class, boolean.class, "equals", Object.class);
		MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
		if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
		{
			MethodGenerator mg = visitMethod(methodTemplate);
			mg.callThisGetter(p_entityEqualsTemplate);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeVirtual(entityEquals_Equals);
			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementHashCodeMethod(PropertyInstance p_entityEqualsTemplate)
	{
		MethodInstance methodTemplate = new MethodInstance(null, Object.class, int.class, "hashCode");
		MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
		if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
		{
			MethodGenerator mg = visitMethod(methodTemplate);
			mg.callThisGetter(p_entityEqualsTemplate);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeVirtual(entityEquals_HashCode);

			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementToStringMethod(PropertyInstance p_entityEqualsTemplate)
	{
		{
			MethodInstance methodTemplate = new MethodInstance(null, Object.class, String.class, "toString");
			MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
			if (method == null || Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
			{
				MethodGenerator mg = visitMethod(methodTemplate);
				mg.callThisGetter(p_entityEqualsTemplate);
				mg.loadThis();
				mg.loadThis();
				mg.invokeVirtual(entityEquals_toString_Obj);
				mg.returnValue();
				mg.endMethod();
			}
		}

		{
			MethodInstance methodTemplate = new MethodInstance(null, IPrintable.class, void.class, "toString", StringBuilder.class);
			MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
			if (method == null || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
			{
				MethodGenerator mg = visitMethod(methodTemplate);
				mg.callThisGetter(p_entityEqualsTemplate);
				mg.loadThis();
				mg.loadArgs();
				mg.invokeVirtual(entityEquals_toString_Printable);
				mg.returnValue();
				mg.endMethod();
			}
		}
	}
}
