using De.Osthus.Ambeth.Metadata;
using System;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityMetaDataEmbeddedMemberVisitor : ClassVisitor
    {
	    protected static readonly MethodInstance template_m_getMemberPath = new MethodInstance(null, typeof(IEmbeddedMember), typeof(Member[]), "GetMemberPath");

	    protected static readonly MethodInstance template_m_getMemberPathString = new MethodInstance(null, typeof(IEmbeddedMember), typeof(String), "GetMemberPathString");

	    protected static readonly MethodInstance template_m_getMemberPathToken = new MethodInstance(null, typeof(IEmbeddedMember), typeof(String[]), "GetMemberPathToken");

	    protected static readonly MethodInstance template_m_getChildMember = new MethodInstance(null, typeof(IEmbeddedMember), typeof(Member), "GetChildMember");

	    protected readonly Type entityType;

	    protected readonly String memberName;

	    protected readonly Member[] members;

	    public EntityMetaDataEmbeddedMemberVisitor(IClassVisitor cv, Type entityType, String memberName, Member[] members) : base(new InterfaceAdder(cv, typeof(IEmbeddedMember)))
	    {		    
		    this.entityType = entityType;
		    this.memberName = memberName;
		    this.members = members;
	    }

	    public override void VisitEnd()
	    {
		    String[] memberNameSplit = EmbeddedMember.Split(memberName);
		    Member[] memberPath = new Member[members.Length - 1];
		    Array.Copy(members, 0, memberPath, 0, memberPath.Length);
		    ImplementGetMemberPath(memberPath);
		    ImplementGetMemberPathString(EmbeddedMember.BuildMemberPathString(members));
		    ImplementGetMemberPathToken(memberNameSplit);
		    ImplementGetChildMember(members[members.Length - 1]);
		    base.VisitEnd();
	    }

	    protected void ImplementGetMemberPath(Member[] memberPath)
	    {
		    FieldInstance f_memberPath = ImplementStaticAssignedField("sf__memberPath", memberPath);
		    ImplementGetter(template_m_getMemberPath, f_memberPath);
	    }

	    protected void ImplementGetMemberPathString(String memberPathString)
	    {
		    FieldInstance f_memberPathString = ImplementStaticAssignedField("sf__memberPathString", memberPathString);
		    ImplementGetter(template_m_getMemberPathString, f_memberPathString);
	    }

	    protected void ImplementGetMemberPathToken(String[] memberPathSplit)
	    {
		    FieldInstance f_memberPathToken = ImplementStaticAssignedField("sf__memberPathToken", memberPathSplit);
		    ImplementGetter(template_m_getMemberPathToken, f_memberPathToken);
	    }

	    protected void ImplementGetChildMember(Member childMember)
	    {
		    FieldInstance f_childMember = ImplementStaticAssignedField("sf__childMember", childMember);
		    ImplementGetter(template_m_getChildMember, f_childMember);
	    }
    }
}