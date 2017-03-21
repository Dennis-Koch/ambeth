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
import com.koch.ambeth.cache.proxy.IEntityEquals;
import com.koch.ambeth.merge.bytecode.visitor.EntityMetaDataMemberVisitor;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.PrimitiveMember;

public class GetIdMethodCreator extends ClassGenerator
{
	protected final IEntityMetaData metaData;

	private static final MethodInstance template_m_entityEquals_getId = new MethodInstance(null, IEntityEquals.class, Object.class, "get__Id");

	public static MethodInstance getGetId()
	{
		return MethodInstance.findByTemplate(template_m_entityEquals_getId, false);
	}

	public GetIdMethodCreator(ClassVisitor cv, IEntityMetaData metaData)
	{
		super(cv);
		this.metaData = metaData;
	}

	@Override
	public void visitEnd()
	{
		MethodInstance m_get__Id = MethodInstance.findByTemplate(GetIdMethodCreator.template_m_entityEquals_getId, true);
		if (m_get__Id != null)
		{
			super.visitEnd();
			return;
		}
		MethodInstance m_getEntityMetaData = EntityMetaDataHolderVisitor.getImplementedGetEntityMetaData(this, metaData);
		MethodGenerator mg = visitMethod(GetIdMethodCreator.template_m_entityEquals_getId);
		mg.callThisGetter(m_getEntityMetaData);
		mg.invokeInterface(new MethodInstance(null, IEntityMetaData.class, PrimitiveMember.class, "getIdMember"));
		mg.loadThis();
		mg.push(false);
		mg.invokeVirtual(EntityMetaDataMemberVisitor.template_m_getValueWithFlag);
		mg.returnValue();
		mg.endMethod();

		super.visitEnd();
	}

	protected Type getDeclaringType(java.lang.reflect.Member member, Type newEntityType)
	{
		if (member.getDeclaringClass().isInterface())
		{
			return newEntityType;
		}
		return Type.getType(member.getDeclaringClass());
	}
}
