using De.Osthus.Ambeth.Util;
using System;
using System.IO;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToFileConverter : IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            if (expectedType.IsAssignableFrom(typeof(FileInfo)))
            {
                return new FileInfo((String)value);
            }
            return new DirectoryInfo((String)value);
        }
    }
}