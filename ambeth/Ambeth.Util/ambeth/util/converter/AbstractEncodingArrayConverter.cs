using System;

namespace De.Osthus.Ambeth.Util.Converter
{
    public class AbstractEncodingArrayConverter
    {
        protected EncodingType GetSourceEncoding(Object additionalInformation)
        {
            EncodingType sourceEncoding = EncodingType.PLAIN;
            if (additionalInformation is int)
            {
                int encoding = (int)additionalInformation;
                sourceEncoding = EncodingInformation.GetSourceEncoding(encoding);
            }
            return sourceEncoding;
        }

        protected EncodingType GetTargetEncoding(Object additionalInformation)
        {
            EncodingType targetEncoding = EncodingType.PLAIN;
            if (additionalInformation is int)
            {
                int encoding = (int)additionalInformation;
                targetEncoding = EncodingInformation.GetTargetEncoding(encoding);
            }
            return targetEncoding;
        }

        protected byte[] SwitchEncoding(byte[] bytes, EncodingType sourceEncoding, EncodingType targetEncoding)
        {
            if (sourceEncoding.Equals(targetEncoding))
            {
                return bytes;
            }
            switch (sourceEncoding)
            {
                case EncodingType.PLAIN:
                    {
                        // Nothing to do
                        break;
                    }
                case EncodingType.BASE64:
                    {
                        bytes = Base64.DecodeBase64(bytes);
                        break;
                    }
                default:
                    {
                        throw new NotSupportedException(sourceEncoding + " not yet supported");
                    }
            }
            switch (targetEncoding)
            {
                case EncodingType.PLAIN:
                    {
                        // Nothing to do
                        break;
                    }
                case EncodingType.BASE64:
                    {
                        bytes = Base64.EncodeBase64(bytes);
                        break;
                    }
                default:
                    {
                        throw new NotSupportedException(sourceEncoding + " not yet supported");
                    }
            }
            return bytes;
        }
    }
}