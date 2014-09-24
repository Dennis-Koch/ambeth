using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Runtime.CompilerServices;
using System.Text;

namespace De.Osthus.Ambeth.Template
{
    public class EntityEqualsTemplate
    {
        public bool Equals(IEntityEquals left, Object right)
        {
            if (right == left)
            {
                return true;
            }
            if (!(right is IEntityEquals))
            {
                return false;
            }
            Object id = left.Get__Id();
            if (id == null)
            {
                // Null id can never be equal with something other than itself
                return false;
            }
            IEntityEquals other = (IEntityEquals)right;
            return id.Equals(other.Get__Id()) && left.Get__BaseType().Equals(other.Get__BaseType());
        }

        public int GetHashCode(IEntityEquals left)
        {
            Object id = left.Get__Id();
            if (id == null)
            {
                return RuntimeHelpers.GetHashCode(left);
            }
            return left.Get__BaseType().GetHashCode() ^ id.GetHashCode();
        }

        public String ToString(IEntityEquals left, IPrintable printable)
        {
            StringBuilder sb = new StringBuilder();
            printable.ToString(sb);
            return sb.ToString();
        }

        public void ToString(IEntityEquals left, StringBuilder sb)
        {
            sb.Append(left.Get__BaseType().FullName).Append('-');
            StringBuilderUtil.AppendPrintable(sb, left.Get__Id());
        }
    }
}
