package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

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
