using System;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchPath
    {
        public readonly int memberIndex;

        public readonly String memberName;

        public readonly Type memberType;

        public readonly PrefetchPath[] children;

		public readonly Type[] memberTypesOnDescendants;

		public PrefetchPath(Type memberType, int memberIndex, String memberName, PrefetchPath[] children, Type[] memberTypesOnDescendants)
        {
            this.memberIndex = memberIndex;
            this.memberName = memberName;
            this.memberType = memberType;
            this.children = children;
			this.memberTypesOnDescendants = memberTypesOnDescendants;
        }

        public override String ToString()
        {
            return memberName;
        }
    }
}