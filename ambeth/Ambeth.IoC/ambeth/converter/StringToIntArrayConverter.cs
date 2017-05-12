using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToIntArrayConverter : IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (typeof(int[]).Equals(expectedType))
            {
                String[] split = StringToPatternConverter.splitPattern.Split((String)value);
                int[] result = new int[split.Length];
                for (int a = split.Length; a-- > 0; )
                {
                    result[a] = Int32.Parse(split[a]);
                }
                return result;
            }
            StringBuilder sb = new StringBuilder();
            int[] array = (int[])value;
            foreach (int item in array)
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
