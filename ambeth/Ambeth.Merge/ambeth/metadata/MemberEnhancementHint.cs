using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class MemberEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint
    {
        protected readonly Type declaringType;

        protected readonly String memberName;

        public MemberEnhancementHint(Type declaringType, String memberName)
        {
            this.declaringType = declaringType;
            this.memberName = memberName;
        }

        public Type DeclaringType
        {
            get
            {
                return declaringType;
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
            return DeclaringType.Equals(other.DeclaringType) && MemberName.Equals(other.MemberName);
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ DeclaringType.GetHashCode() ^ MemberName.GetHashCode();
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
            return declaringType.FullName + "$" + typeof(Member).Name + "$" + memberName.Replace('.','_');
        }

        public override string ToString()
        {
            return GetType().Name + ": Path=" + declaringType.Name + "." + memberName;
        }
    }
}