using System;

namespace De.Osthus.Ambeth.Exceptions
{
    public class ReconnectException : Exception
    {
        public ReconnectException()
            : base()
        {
        }

        public ReconnectException(String message)
            : base(message)
        {
        }

        public ReconnectException(String message, Exception innerException)
            : base(message, innerException)
        {
        }
    }
}
