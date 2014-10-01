using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class IntermediateEmbeddedPrimitiveMember : IntermediatePrimitiveMember, IEmbeddedMember
    {
        protected readonly Member[] memberPath;

        protected readonly PrimitiveMember childMember;

        protected readonly String[] memberPathToken;

        protected readonly String memberPathString;

        public IntermediateEmbeddedPrimitiveMember(Type entityType, Type realType, Type elementType, String propertyName, Member[] memberPath, PrimitiveMember childMember)
            : base(entityType, entityType, realType, elementType, propertyName, null)
        {
            this.memberPath = memberPath;
            this.childMember = childMember;
            this.memberPathToken = EmbeddedMember.BuildMemberPathToken(memberPath);
            this.memberPathString = EmbeddedMember.BuildMemberPathString(memberPath);
        }

        public override bool IsToMany
	    {
            get
            {
		        return childMember.IsToMany;
            }
	    }

        public override Attribute GetAnnotation(Type annotationType)
	    {
		    return childMember.GetAnnotation(annotationType);
	    }

	    public override bool TechnicalMember
	    {
            get
            {
                return childMember.TechnicalMember;
            }
	    }

	    public override void SetTechnicalMember(bool technicalMember)
	    {
		    ((IPrimitiveMemberWrite) childMember).SetTechnicalMember(technicalMember);
	    }

        public Member[] GetMemberPath()
        {
            return memberPath;
        }

        public String GetMemberPathString()
        {
            return memberPathString;
        }

        public String[] GetMemberPathToken()
        {
            return memberPathToken;
        }

        public Member ChildMember
        {
            get
            {
                return childMember;
            }
        }
    }
}
