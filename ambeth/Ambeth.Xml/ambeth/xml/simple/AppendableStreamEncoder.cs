using System;
using System.IO;
using System.Text;
namespace De.Osthus.Ambeth.Xml.Simple
{
    public class AppendableStreamEncoder : IAppendable
    {
        protected readonly Stream se;

        protected readonly Encoding encoding;

        public AppendableStreamEncoder(Stream se, Encoding encoding)
        {
            this.se = se;
            this.encoding = encoding;
        }

        public IAppendable Append(char value)
        {
            byte oneByte = Convert.ToByte(value);
            se.WriteByte(oneByte);
            return this;
        }

        public IAppendable Append(String value)
        {
            byte[] bytes = encoding.GetBytes(value);
            se.Write(bytes, 0, bytes.Length);
            return this;
        }
    }
}