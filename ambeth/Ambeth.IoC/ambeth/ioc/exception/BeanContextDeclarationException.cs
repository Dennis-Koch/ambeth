using System;
using System.Diagnostics;
using System.Text;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class BeanContextDeclarationException : Exception
    {
        private readonly StackFrame[] stackTrace;

        public BeanContextDeclarationException(StackFrame[] stackTrace)
            : this(stackTrace, null)
        {
            // Intended blank
        }

        public BeanContextDeclarationException(StackFrame[] stackTrace, Exception cause)
            : base("Declaration at", cause)
        {
            this.stackTrace = stackTrace;
        }

        public override String StackTrace
        {
            get
            {
                StringBuilder sb = new StringBuilder();
                bool first = true;
                foreach (StackFrame frame in stackTrace)
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        sb.Append(Environment.NewLine);
                    }
                    sb.Append("\tat ");
                    sb.Append(frame.ToString());
                }
                return sb.ToString();
            }
        }
    }
}