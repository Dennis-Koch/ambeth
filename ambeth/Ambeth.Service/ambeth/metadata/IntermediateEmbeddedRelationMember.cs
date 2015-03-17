using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class IntermediateEmbeddedRelationMember : IntermediateRelationMember, IEmbeddedMember
    {
        protected readonly Member[] memberPath;

        protected readonly Member childMember;

        protected readonly String[] memberPathToken;

        protected readonly String memberPathString;

        public IntermediateEmbeddedRelationMember(Type entityType, Type realType, Type elementType, String propertyName, Member[] memberPath, Member childMember)
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
