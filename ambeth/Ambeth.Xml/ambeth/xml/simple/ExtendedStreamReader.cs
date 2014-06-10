using System.IO;
using System.Text;

namespace De.Osthus.Ambeth.Xml.Simple
{
    public class ExtendedStreamReader : StreamReader
    {
        protected readonly StringBuilder sb;

        public ExtendedStreamReader(Stream stream, StringBuilder sb)
            : base(stream)
        {
            this.sb = sb;
        }

        public override int Read()
        {
            return base.Read();
        }

        public override int Read(char[] buffer, int index, int count)
        {
            return base.Read(buffer, index, count);
        }
    }
}