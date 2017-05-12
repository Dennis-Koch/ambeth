using System;

namespace De.Osthus.Ambeth.Exceptions
{
    public class MaskingRuntimeException : Exception
    {
        protected readonly String stackTrace;

        public MaskingRuntimeException(String message, String stackTrace, Exception cause) : base(message, cause)
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
