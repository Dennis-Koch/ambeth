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

        public static bool Equals<T>(T[] left, T[] right)
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
                if (!Object.Equals(left[a], right[a]))
                {
                    return false;
                }
            }
            return true;
        }

        public static int GetHashCode<T>(T[] left)
        {
            if (left == null)
            {
                return 0;
            }

            int result = 1;

            foreach (T element in left)
            {
                result = 31 * result + (element == null ? 0 : element.GetHashCode());
            }
            return result;
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