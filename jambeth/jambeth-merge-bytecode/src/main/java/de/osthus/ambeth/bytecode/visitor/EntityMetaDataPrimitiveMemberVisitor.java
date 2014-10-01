package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.metadata.IPrimitiveMemberWrite;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.typeinfo.IPropertyInfo;

public class EntityMetaDataPrimitiveMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_isTechnicalMember = new MethodInstance(null, PrimitiveMember.class, boolean.class, "isTechnicalMember");

	protected static final MethodInstance template_m_setTechnicalMember = new MethodInstance(null, IPrimitiveMemberWrite.class, void.class,
			"setTechnicalMember", boolean.class);

	protected final Class<?> entityType;

	protected final String memberName;

	protected IPropertyInfo[] propertyPath;

	public EntityMetaDataPrimitiveMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, IPropertyInfo[] propertyPath)
	{
		super(new InterfaceAdder(cv, IPrimitiveMemberWrite.class));
		this.entityType = entityType;
		this.memberName = memberName;
		this.propertyPath = propertyPath;
	}

	@Override
	public void visitEnd()
	{
		implementIsTechnicalMember();
		super.visitEnd();
	}

	protected void implementIsTechnicalMember()
	{
		FieldInstance f_technicalMember = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__technicalMember", null, boolean.class));

		implementGetter(template_m_isTechnicalMember, f_technicalMember);
		implementSetter(template_m_setTechnicalMember, f_technicalMember);
	}
}
