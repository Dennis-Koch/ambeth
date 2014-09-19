using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class MemberEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint
    {
        protected readonly Type entityType;

        protected readonly String memberName;

        public MemberEnhancementHint(Type entityType, String memberName)
        {
            this.entityType = entityType;
            this.memberName = memberName;
        }

        public Type EntityType
        {
            get
            {
                return entityType;
            }
        }

        public String MemberName
        {
            get
            {
                return memberName;
            }
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!GetType().Equals(obj.GetType()))
            {
                return false;
            }
            MemberEnhancementHint other = (MemberEnhancementHint)obj;
            return EntityType.Equals(other.EntityType) && MemberName.Equals(other.MemberName);
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ EntityType.GetHashCode() ^ MemberName.GetHashCode();
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public virtual Object Unwrap(Type includedHintType)
        {
            if (typeof(MemberEnhancementHint).Equals(includedHintType))
            {
                return this;
            }
            return null;
        }

        public virtual String GetTargetName(Type typeToEnhance)
        {
            return entityType.FullName + "$" + typeof(Member).Name + "$" + memberName;
        }
    }
}