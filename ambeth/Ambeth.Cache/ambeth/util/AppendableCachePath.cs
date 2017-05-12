using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Util
{
    public class AppendableCachePath
    {
        public readonly int memberIndex;

        public readonly String memberName;

        public readonly Type memberType;

        public CHashSet<AppendableCachePath> children;

        public AppendableCachePath(Type memberType, int memberIndex, String memberName)
	    {
		    this.memberIndex = memberIndex;
		    this.memberName = memberName;
		    this.memberType = memberType;
	    }
    }
}