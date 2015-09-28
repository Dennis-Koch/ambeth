using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToDoubleArrayConverter : IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (typeof(double[]).Equals(expectedType))
            {
                String[] split = StringToPatternConverter.splitPattern.Split((String)value);
                double[] result = new double[split.Length];
                for (int a = split.Length; a-- > 0; )
                {
                    result[a] = Double.Parse(split[a]);
                }
                return result;
            }
            StringBuilder sb = new StringBuilder();
            double[] array = (double[])value;
            foreach (double item in array)
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
