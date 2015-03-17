using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public delegate void StringBuilderDelegate(StringBuilder sb);
    
    public sealed class StringBuilderUtil
    {
        [ThreadStatic]
        private static StringBuilder sb;

        public static StringBuilder StringBuilderProp
        {
            get
            {
                if (sb == null)
                {
                    sb = new StringBuilder();
                }
                return sb;
            }
        }

        public static void AppendTabs(StringBuilder sb, int tabCount)
	    {
		    for (int a = tabCount; a-- > 0;)
		    {
			    sb.Append('\t');
		    }
	    }

        private StringBuilderUtil()
        {
        }

        public static String UpperCaseFirst(String s)
        {
            if (s == null || s.Length == 0)
            {
                return "";
            }
            char firstChar = s[0];
            if (Char.IsUpper(firstChar))
            {
                return s;
            }
            return StringBuilderUtil.Concat(delegate(StringBuilder sb)
            {
                sb.Append(Char.ToUpperInvariant(firstChar));
                sb.Append(s.Substring(1));
            });
        }

        public static String LowerCaseFirst(String s)
        {
            if (s == null || s.Length == 0)
            {
                return "";
            }
            char firstChar = s[0];
            if (Char.IsLower(firstChar))
            {
                return s;
            }
            return StringBuilderUtil.Concat(delegate(StringBuilder sb)
            {
                sb.Append(Char.ToLowerInvariant(firstChar));
                sb.Append(s.Substring(1));
            });
        }

        public static String Concat(StringBuilderDelegate sbDelegate)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sbDelegate(sb);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5);
                return sb.ToString();
            }
            finally
            { 
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6, String s7)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6).Append(s7);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6).Append(s7).Append(s8);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8,
                String s9)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6).Append(s7).Append(s8);
                sb.Append(s9);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8,
                String s9, String s10)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6).Append(s7).Append(s8);
                sb.Append(s9).Append(s10);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8,
                String s9, String s10, String s11)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6).Append(s7).Append(s8);
                sb.Append(s9).Append(s10).Append(s11);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static String Concat(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8,
                String s9, String s10, String s11, String s12)
        {
            StringBuilder sb = StringBuilderProp;
            try
            {
                sb.Append(s1).Append(s2).Append(s3).Append(s4).Append(s5).Append(s6).Append(s7).Append(s8);
                sb.Append(s9).Append(s10).Append(s11).Append(s12);
                return sb.ToString();
            }
            finally
            {
                sb.Length = 0;
            }
        }

        public static void AppendPrintable(StringBuilder sb, Object printable)
        {
            if (printable == null)
            {
                sb.Append("null");
            }
            else if (printable is IPrintable)
            {
                ((IPrintable)printable).ToString(sb);
            }
            else if (printable.GetType().IsArray && typeof(Object).Equals(printable.GetType().GetElementType()))
            {
                Arrays.ToString(sb, (Object[])printable);
            }
            else
            {
                sb.Append(printable);
            }
        }
    }
}