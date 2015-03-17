using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.IO;
using System.Text;

namespace De.Osthus.Ambeth.Converter
{
    public class StringToFileConverter : IDedicatedConverter
    {
        [LogInstance]
        public ILogger Log { private get; set; }

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