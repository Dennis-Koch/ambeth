package com.koch.ambeth.cache.bytecode.visitor;

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
