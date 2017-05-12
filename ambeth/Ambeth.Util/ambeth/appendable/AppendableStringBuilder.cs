using System;
using System.Text;

namespace De.Osthus.Ambeth.Appendable
{
    public class AppendableStringBuilder : IAppendable
    {
        protected readonly StringBuilder sb;

        public AppendableStringBuilder(StringBuilder sb)
        {
            this.sb = sb;
        }

        public IAppendable Append(char value)
        {
            sb.Append(value);
            return this;
        }

        public IAppendable Append(String value)
        {
            sb.Append(value);
            return this;
        }
    }
}