using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using System;
namespace De.Osthus.Ambeth.Util
{
    public class ValueHolderRef
    {
        protected IObjRef objRef;

        protected IRelationInfoItem member;

        public ValueHolderRef(IObjRef objRef, IRelationInfoItem member)
        {
            this.objRef = objRef;
            this.member = member;
        }

        public IObjRef ObjRef
        {
            get
            {
                return objRef;
            }
        }

        public IRelationInfoItem Member
        {
            get
            {
                return member;
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
            return Object.Equals(ObjRef, other.ObjRef) && Object.Equals(Member, other.Member);
        }

        public override int GetHashCode()
        {
            return ObjRef.GetHashCode() ^ Member.GetHashCode();
        }
    }
}