using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Template
{
    public class CompositeIdTemplate
    {
        public bool EqualsCompositeId(ITypeInfoItem[] members, Object left, Object right)
        {
            if (left == null || right == null)
            {
                return false;
            }
            if (left == right)
            {
                return true;
            }
            if (!left.GetType().Equals(right.GetType()))
            {
                return false;
            }
            foreach (ITypeInfoItem member in members)
            {
                Object leftValue = member.GetValue(left, false);
                Object rightValue = member.GetValue(right, false);
                if (leftValue == null || rightValue == null)
                {
                    return false;
                }
                if (!leftValue.Equals(rightValue))
                {
                    return false;
                }
            }
            return true;
        }

        public int HashCodeCompositeId(ITypeInfoItem[] members, Object compositeId)
        {
            int hash = compositeId.GetType().GetHashCode();
            foreach (ITypeInfoItem member in members)
            {
                Object value = member.GetValue(compositeId, false);
                if (value != null)
                {
                    hash ^= value.GetHashCode();
                }
            }
            return hash;
        }

        public String ToStringCompositeId(ITypeInfoItem[] members, Object compositeId)
        {
            StringBuilder sb = new StringBuilder();
            ToStringSbCompositeId(members, compositeId, sb);
            return sb.ToString();
        }

        public void ToStringSbCompositeId(ITypeInfoItem[] members, Object compositeId, StringBuilder sb)
        {
            // order does matter here
            for (int a = 0, size = members.Length; a < size; a++)
            {
                Object value = members[a].GetValue(compositeId);
                if (a > 0)
                {
                    sb.Append('#');
                }
                if (value != null)
                {
                    StringBuilderUtil.AppendPrintable(sb, value);
                }
                else
                {
                    sb.Append("<null>");
                }
            }
        }
    }
}