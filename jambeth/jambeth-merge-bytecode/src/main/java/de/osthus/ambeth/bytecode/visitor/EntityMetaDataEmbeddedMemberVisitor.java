package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.metadata.EmbeddedMember;
import de.osthus.ambeth.metadata.IEmbeddedMember;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class EntityMetaDataEmbeddedMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_getMemberPath = new MethodInstance(null, IEmbeddedMember.class, Member[].class, "getMemberPath");

	protected static final MethodInstance template_m_getMemberPathString = new MethodInstance(null, IEmbeddedMember.class, String.class, "getMemberPathString");

	protected static final MethodInstance template_m_getMemberPathToken = new MethodInstance(null, IEmbeddedMember.class, String[].class, "getMemberPathToken");

	protected static final MethodInstance template_m_getChildMember = new MethodInstance(null, IEmbeddedMember.class, Member.class, "getChildMember");

	protected final Class<?> entityType;

	protected final String memberName;

	protected final Member[] members;

	public EntityMetaDataEmbeddedMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, Member[] members)
	{
		super(new InterfaceAdder(cv, IEmbeddedMember.class));
		this.entityType = entityType;
		this.memberName = memberName;
		this.members = members;
	}

	@Override
	public void visitEnd()
	{
		String[] memberNameSplit = EmbeddedMember.split(memberName);
		Member[] memberPath = new Member[members.length - 1];
		System.arraycopy(members, 0, memberPath, 0, memberPath.length);
		implementGetMemberPath(memberPath);
		implementGetMemberPathString(EmbeddedMember.buildMemberPathString(members));
		implementGetMemberPathToken(memberNameSplit);
		implementGetChildMember(members[members.length - 1]);
		super.visitEnd();
	}

	protected void implementGetMemberPath(Member[] memberPath)
	{
		FieldInstance f_memberPath = implementStaticAssignedField("sf__memberPath", memberPath);
		implementGetter(template_m_getMemberPath, f_memberPath);
	}

	protected void implementGetMemberPathString(String memberPathString)
	{
		FieldInstance f_memberPathString = implementStaticAssignedField("sf__memberPathString", memberPathString);
		implementGetter(template_m_getMemberPathString, f_memberPathString);
	}

	protected void implementGetMemberPathToken(String[] memberPathSplit)
	{
		FieldInstance f_memberPathToken = implementStaticAssignedField("sf__memberPathToken", memberPathSplit);
		implementGetter(template_m_getMemberPathToken, f_memberPathToken);
	}

	protected void implementGetChildMember(Member childMember)
	{
		FieldInstance f_childMember = implementStaticAssignedField("sf__childMember", childMember);
		implementGetter(template_m_getChildMember, f_childMember);
	}
}
