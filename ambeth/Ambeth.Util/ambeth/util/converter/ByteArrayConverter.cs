using System;
using System.Text;

namespace De.Osthus.Ambeth.Util.Converter
{
    public class ByteArrayConverter : AbstractEncodingArrayConverter, IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            EncodingType sourceEncoding = GetSourceEncoding(additionalInformation);
            EncodingType targetEncoding = GetTargetEncoding(additionalInformation);

            if (typeof(byte[]).Equals(sourceType) && typeof(String).Equals(expectedType))
            {
                byte[] bytes = SwitchEncoding((byte[])value, sourceEncoding, targetEncoding);
                String text = Encoding.UTF8.GetString(bytes, 0, bytes.Length);
                return text;
            }
            else if (typeof(String).Equals(sourceType) && typeof(byte[]).Equals(expectedType))
            {
                byte[] bytes = Encoding.UTF8.GetBytes((String)value);
                bytes = SwitchEncoding(bytes, sourceEncoding, targetEncoding);
                return bytes;
            }
            throw new Exception("Conversion " + sourceType.FullName + "->" + expectedType.FullName + " not supported");
        }
    }
}