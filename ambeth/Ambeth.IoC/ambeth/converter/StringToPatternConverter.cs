using De.Osthus.Ambeth.Util;
using System;
using System.Text;
using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToPatternConverter : IDedicatedConverter
    {
        public static readonly Regex splitPattern = new Regex(";");
		
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (typeof(Regex).Equals(expectedType))
            {
                return new Regex((String)value);
            }
            else if (typeof(Regex[]).Equals(expectedType))
            {
                String[] split = splitPattern.Split((String)value);
                Regex[] patterns = new Regex[split.Length];
                for (int a = split.Length; a-- > 0; )
                {
                    patterns[a] = new Regex(split[a]);
                }
                return patterns;
            }
            else
            {
                if (typeof(Regex).Equals(sourceType))
                {
                    return ((Regex)value).ToString();
                }
                else
                {
                    StringBuilder sb = new StringBuilder();
                    Regex[] patterns = (Regex[])value;
                    foreach (Regex pattern in patterns)
                    {
                        if (sb.Length > 0)
                        {
                            sb.Append(splitPattern.ToString());
                        }
                        sb.Append(pattern.ToString());
                    }
                    return sb.ToString();
                }
            }
        }
    }
}