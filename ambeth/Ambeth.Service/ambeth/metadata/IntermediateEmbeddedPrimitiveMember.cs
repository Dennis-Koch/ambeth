using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class IntermediateEmbeddedPrimitiveMember : IntermediatePrimitiveMember, IEmbeddedMember
    {
        protected readonly Member[] memberPath;

        protected readonly Member childMember;

        protected readonly String[] memberPathToken;

        protected readonly String memberPathString;

        public IntermediateEmbeddedPrimitiveMember(Type type, Type realType, Type elementType, String propertyName, Member[] memberPath, Member childMember)
            : base(type, realType, elementType, propertyName)
        {
            this.memberPath = memberPath;
            this.childMember = childMember;
            this.memberPathToken = EmbeddedMember.BuildMemberPathToken(memberPath);
            this.memberPathString = EmbeddedMember.BuildMemberPathString(memberPath);
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
