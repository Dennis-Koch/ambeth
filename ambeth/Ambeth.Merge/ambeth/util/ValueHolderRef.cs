using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Util
{
    public class ValueHolderRef
    {
        protected IObjRef objRef;

        protected RelationMember member;

		protected int relationIndex;

		public ValueHolderRef(IObjRef objRef, RelationMember member, int relationIndex)
		{
			this.objRef = objRef;
			this.member = member;
			this.relationIndex = relationIndex;
		}

        public IObjRef ObjRef
        {
            get
            {
                return objRef;
            }
        }

        public RelationMember Member
        {
            get
            {
                return member;
            }
        }

		public int RelationIndex
		{
			get
			{
				return relationIndex;
			}
		}

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is ValueHolderRef))
            {
                return false;
            }
            ValueHolderRef other = (ValueHolderRef)obj;
			return Object.Equals(ObjRef, other.ObjRef) && RelationIndex == other.RelationIndex;
        }

        public override int GetHashCode()
        {
			return ObjRef.GetHashCode() ^ RelationIndex;
        }
    }
}