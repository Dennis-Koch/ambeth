using System;

namespace De.Osthus.Ambeth.Util
{
    public class CachePath
    {
        public readonly int memberIndex;

        public readonly String memberName;

        public readonly Type memberType;

        public readonly CachePath[] children;

        public CachePath(Type memberType, int memberIndex, String memberName, CachePath[] children)
        {
            this.memberIndex = memberIndex;
            this.memberName = memberName;
            this.memberType = memberType;
            this.children = children;
        }

        public override String ToString()
        {
            return memberName;
        }
    }
}