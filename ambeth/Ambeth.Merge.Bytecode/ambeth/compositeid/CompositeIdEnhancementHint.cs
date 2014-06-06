using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.CompositeId
{
    public class CompositeIdEnhancementHint : IEnhancementHint, ITargetNameEnhancementHint, IPrintable
    {
        private readonly ITypeInfoItem[] idMembers;

        public CompositeIdEnhancementHint(ITypeInfoItem[] idMembers)
        {
            this.idMembers = idMembers;
        }

        public String GetTargetName(Type typeToEnhance)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(GetType().Namespace).Append('.').Append("CompositeId");
            for (int a = 0, size = idMembers.Length; a < size; a++)
            {
                ITypeInfoItem idMember = idMembers[a];
                sb.Append('$').Append(idMember.Name);
            }
            return sb.ToString();
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is CompositeIdEnhancementHint))
            {
                return false;
            }
            CompositeIdEnhancementHint other = (CompositeIdEnhancementHint)obj;
            if (other.idMembers.Length != idMembers.Length)
            {
                return false;
            }
            for (int a = idMembers.Length; a-- > 0; )
            {
                ITypeInfoItem idMember = idMembers[a];
                ITypeInfoItem otherIdMember = other.idMembers[a];
                if (!Object.Equals(idMember.Name, otherIdMember.Name) || !Object.Equals(idMember.RealType, otherIdMember.RealType))
                {
                    return false;
                }
            }
            return true;
        }

        public override int GetHashCode()
        {
            int hash = typeof(CompositeIdEnhancementHint).GetHashCode();
            for (int a = idMembers.Length; a-- > 0; )
            {
                ITypeInfoItem idMember = idMembers[a];
                hash ^= idMember.Name.GetHashCode() ^ idMember.RealType.GetHashCode();
            }
            return hash;
        }

        public ITypeInfoItem[] IdMembers
        {
            get
            {
                return idMembers;
            }
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedContextType)
        {
            if (typeof(CompositeIdEnhancementHint).IsAssignableFrom(includedContextType))
            {
                return this;
            }
            return null;
        }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append(GetType().FullName).Append(": ");
            for (int a = 0, size = idMembers.Length; a < size; a++)
            {
                if (a > 0)
                {
                    sb.Append(',');
                }
                sb.Append(idMembers[a].Name);
            }
        }
    }
}