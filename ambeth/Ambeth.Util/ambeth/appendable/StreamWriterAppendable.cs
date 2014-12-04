using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace De.Osthus.Ambeth.Appendable
{
    public class StreamWriterAppendable : IAppendable
    {
        protected StreamWriter streamWriter;

        public StreamWriterAppendable(StreamWriter streamWriter)
        {
            this.streamWriter = streamWriter;
        }

        public IAppendable Append(String value)
        {
            streamWriter.Write(value);
            return this;
        }

        public IAppendable Append(char value)
        {
            streamWriter.Write(value);
            return this;
        }
    }
}
