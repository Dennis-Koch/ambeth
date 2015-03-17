using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToClassArrayConverter : IDedicatedConverter
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (typeof(Type[]).Equals(expectedType))
            {
                String[] split = StringToPatternConverter.splitPattern.Split((String)value);
                Type[] result = new Type[split.Length];
                for (int a = split.Length; a-- > 0; )
                {
                    result[a] = ConversionHelper.ConvertValueToType<Type>(split[a]);
                }
                return result;
            }
            StringBuilder sb = new StringBuilder();
            Type[] array = (Type[])value;
            foreach (Type item in array)
            {
                if (sb.Length > 0)
                {
                    sb.Append(StringToPatternConverter.splitPattern.ToString());
                }
                sb.Append(item.FullName);
            }
            return sb.ToString();
        }
    }
}