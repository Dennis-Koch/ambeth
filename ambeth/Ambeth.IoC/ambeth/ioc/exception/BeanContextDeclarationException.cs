using De.Osthus.Ambeth.Exceptions;
using System;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class BeanContextDeclarationException : Exception
    {
        private readonly String stackTrace;

        public BeanContextDeclarationException(String stackTrace)
            : this(stackTrace, null)
        {
            // Intended blank
        }

        public BeanContextDeclarationException(String stackTrace, Exception cause)
            : base("Declaration at", cause)
        {
            this.stackTrace = stackTrace;
        }

        public override String StackTrace
        {
            get
            {
                return stackTrace;
            }
        }
    }
}