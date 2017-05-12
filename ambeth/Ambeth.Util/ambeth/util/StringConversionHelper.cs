using System;

namespace De.Osthus.Ambeth.Util
{
    public class StringConversionHelper
    {
        public static String UpperCaseFirst(String s)
        {
            if (String.IsNullOrEmpty(s))
            {
                return String.Empty;
            }
            return Char.ToUpper(s[0]) + s.Substring(1);
        }

        public static String LowerCaseFirst(String s)
        {
            if (String.IsNullOrEmpty(s))
            {
                return String.Empty;
            }
            return Char.ToLower(s[0]) + s.Substring(1);
        }

        public static String PackageNameToNameSpace(String packageName)
        {
            String[] parts = packageName.Split('.');

            for (int i = parts.Length; i-- > 0; )
            {
                parts[i] = UpperCaseFirst(parts[i]);
            }

            String nameSpace = String.Join(".", parts);
            return nameSpace;
        }
    }
}
