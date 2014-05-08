using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace De.Osthus.Ambeth.Xml
{
    public class StringBuilderAppendable : IAppendable
    {
        protected StringBuilder sb;

        public StringBuilderAppendable(StringBuilder sb)
        {
            this.sb = sb;
        }

        public IAppendable Append(String value)
        {
            sb.Append(value);
            return this;
        }

        public IAppendable Append(char value)
        {
            sb.Append(value);
            return this;
        }
    }
}
