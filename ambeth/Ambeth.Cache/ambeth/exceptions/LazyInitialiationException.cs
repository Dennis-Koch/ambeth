using System;

namespace De.Osthus.Ambeth.Exceptions
{
    public class LazyInitialiationException : Exception
    {
        public LazyInitialiationException(String message) : base(message)
        {
            // Intended blank
        }
    }
}
