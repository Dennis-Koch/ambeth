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
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.merge.proxy.IEnhancedType;

public class GetBaseTypeMethodCreator extends ClassGenerator
{
	private static final MethodInstance template_m_getBaseType = new MethodInstance(null, IEnhancedType.class, Class.class, "get__BaseType");

	public static MethodInstance getGetBaseType()
	{
		return MethodInstance.findByTemplate(template_m_getBaseType, false);
	}

	public GetBaseTypeMethodCreator(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		implementGetBaseType();
		super.visitEnd();
	}

	protected void implementGetBaseType()
	{
		MethodInstance getBaseType = MethodInstance.findByTemplate(template_m_getBaseType, true);
		if (getBaseType != null)
		{
			return;
		}
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		MethodGenerator mg = visitMethod(template_m_getBaseType);
		Type originalType = Type.getType(state.getOriginalType());
		mg.push(originalType);
		mg.returnValue();
		mg.endMethod();
	}
}
