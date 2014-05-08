using De.Osthus.Ambeth.Exceptions;
using System;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class BeanContextInitException : Exception
    {
        protected static Exception ExtractUnmaskedThrowable(Exception cause)
	    {
		    if (cause is MaskingRuntimeException)
		    {
			    if (((MaskingRuntimeException) cause).Message == null)
			    {
				    return ExtractUnmaskedThrowable(cause.InnerException);
			    }
		    }
		    return cause;
	    }

        protected readonly String stackTrace;

        public BeanContextInitException(String message, String stackTrace, Exception cause)
            : base(message, cause)
        {
            this.stackTrace = stackTrace;
        }

        public BeanContextInitException(String message)
            : this(message, null)
        {
            // Intended blank
        }

        public BeanContextInitException(String message, String stackTrace)
            : base(message)
        {
            this.stackTrace = stackTrace;
        }

        public override string StackTrace
        {
            get
            {
                if (stackTrace == null)
                {
                    return base.StackTrace;
                }
                return stackTrace;
            }
        }
    }
}