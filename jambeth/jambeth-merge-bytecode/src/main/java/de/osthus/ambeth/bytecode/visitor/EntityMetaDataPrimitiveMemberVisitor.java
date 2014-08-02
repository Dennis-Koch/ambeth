package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.metadata.IPrimitiveMemberWrite;
import de.osthus.ambeth.metadata.MemberTypeProvider;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

public class EntityMetaDataPrimitiveMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_isTechnicalMember = new MethodInstance(null, PrimitiveMember.class, boolean.class, "isTechnicalMember");

	protected static final MethodInstance template_m_setTechnicalMember = new MethodInstance(null, IPrimitiveMemberWrite.class, void.class,
			"setTechnicalMember", boolean.class);

	protected final Class<?> entityType;

	protected final String memberName;

	protected IPropertyInfoProvider propertyInfoProvider;

	public EntityMetaDataPrimitiveMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
	{
		super(cv);
		this.entityType = entityType;
		this.memberName = memberName;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		IPropertyInfo[] propertyPath = MemberTypeProvider.buildPropertyPath(entityType, memberName, propertyInfoProvider);
		implementIsTechnicalMember(propertyPath);
		super.visitEnd();
	}

	protected void implementIsTechnicalMember(IPropertyInfo[] propertyPath)
	{
		FieldInstance f_technicalMember = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "technicalMember", null, boolean.class));

		implementGetter(template_m_isTechnicalMember, f_technicalMember);
		implementSetter(template_m_setTechnicalMember, f_technicalMember);
	}
}
