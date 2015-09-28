using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToStringArrayConverter : IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (typeof(String[]).Equals(expectedType))
            {
                return StringToPatternConverter.splitPattern.Split((String)value);
            }
            StringBuilder sb = new StringBuilder();
            String[] array = (String[])value;
            foreach (String item in array)
            {
                if (sb.Length > 0)
                {
                    sb.Append(StringToPatternConverter.splitPattern.ToString());
                }
                sb.Append(item);
            }
            return sb.ToString();
        }
    }
}