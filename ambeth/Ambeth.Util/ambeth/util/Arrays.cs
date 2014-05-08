using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public sealed class Arrays
    {
        private Arrays()
        {
        }

        public static bool Equals(Array left, Array right)
        {
            if (left == null)
            {
                return right == null;
            }
            if (right == null)
            {
                return false;
            }
            if (left.Length != right.Length)
            {
                return false;
            }
            for (int a = left.Length; a-- > 0; )
            {
                if (!Object.Equals(left.GetValue(a), right.GetValue(a)))
                {
                    return false;
                }
            }
            return true;
        }

        public static String ToString(Object[] array)
        {
            if (array == null)
            {
                return "null";
            }
            int iMax = array.Length - 1;
            if (iMax == -1)
            {
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            sb.Append('[');
            for (int i = 0; ; i++)
            {
                Object item = array[i];
                StringBuilderUtil.AppendPrintable(sb, item);
                if (i == iMax)
                {
                    return sb.Append(']').ToString();
                }
                sb.Append(", ");
            }
        }

        public static void ToString(StringBuilder sb, Object[] array)
        {
            if (array == null)
            {
                sb.Append("null");
                return;
            }
            int iMax = array.Length - 1;
            if (iMax == -1)
            {
                sb.Append("[]");
                return;
            }
            sb.Append('[');
            for (int i = 0; ; i++)
            {
                Object item = array[i];
                StringBuilderUtil.AppendPrintable(sb, item);
                if (i == iMax)
                {
                    sb.Append(']');
                    return;
                }
                sb.Append(", ");
            }
        }
    }
}
