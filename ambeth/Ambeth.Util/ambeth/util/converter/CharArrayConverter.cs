using System;
using System.Text;

namespace De.Osthus.Ambeth.Util.Converter
{
    public class CharArrayConverter : AbstractEncodingArrayConverter, IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            EncodingType sourceEncoding = GetSourceEncoding(additionalInformation);
            EncodingType targetEncoding = GetTargetEncoding(additionalInformation);

            if (typeof(char[]).Equals(sourceType) && typeof(String).Equals(expectedType))
            {
                byte[] utf8 = Encoding.UTF8.GetBytes((char[])value);
                byte[] bytes = SwitchEncoding(utf8, sourceEncoding, targetEncoding);
                String text = Encoding.UTF8.GetString(bytes, 0, bytes.Length);
                return text;
            }
            else if (typeof(String).Equals(sourceType) && typeof(char[]).Equals(expectedType))
            {
                byte[] utf8 = Encoding.UTF8.GetBytes((String)value);
                byte[] bytes = SwitchEncoding(utf8, sourceEncoding, targetEncoding);
                char[] chars = Encoding.UTF8.GetChars(bytes);
                return chars;
            }
            throw new Exception("Conversion " + sourceType.FullName + "->" + expectedType.FullName + " not supported");
        }
    }
}
