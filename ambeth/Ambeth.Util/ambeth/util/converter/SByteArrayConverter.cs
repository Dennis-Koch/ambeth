using System;
using System.Text;

namespace De.Osthus.Ambeth.Util.Converter
{
    public class SByteArrayConverter : AbstractEncodingArrayConverter, IDedicatedConverter
    {
        public Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation)
        {
            EncodingType sourceEncoding = GetSourceEncoding(additionalInformation);
            EncodingType targetEncoding = GetTargetEncoding(additionalInformation);

            if (typeof(sbyte[]).Equals(sourceType) && typeof(String).Equals(expectedType))
            {
                byte[] unsigned = (byte[])(Array)value;
                byte[] bytes = SwitchEncoding(unsigned, sourceEncoding, targetEncoding);
                String text = Encoding.UTF8.GetString(bytes, 0, bytes.Length);
                return text;
            }
            else if (typeof(String).Equals(sourceType) && typeof(sbyte[]).Equals(expectedType))
            {
                byte[] bytes = Encoding.UTF8.GetBytes((String)value);
                byte[] unsigned = SwitchEncoding(bytes, sourceEncoding, targetEncoding);
                sbyte[] signed = new sbyte[unsigned.Length];
                Buffer.BlockCopy(unsigned, 0, signed, 0, unsigned.Length);
                return signed;
            }
            throw new Exception("Conversion " + sourceType.FullName + "->" + expectedType.FullName + " not supported");
        }
    }
}