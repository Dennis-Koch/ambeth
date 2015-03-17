using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToLongArrayConverter : IDedicatedConverter
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (typeof(long[]).Equals(expectedType))
            {
                String[] split = StringToPatternConverter.splitPattern.Split((String)value);
                long[] result = new long[split.Length];
                for (int a = split.Length; a-- > 0; )
                {
                    result[a] = Int64.Parse(split[a]);
                }
                return result;
            }
            StringBuilder sb = new StringBuilder();
            long[] array = (long[])value;
            foreach (long item in array)
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
