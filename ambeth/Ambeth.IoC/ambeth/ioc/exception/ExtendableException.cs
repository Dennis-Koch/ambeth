using De.Osthus.Ambeth.Ioc.Link;
using System;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class ExtendableException : System.Exception
    {
        public ExtendableException(String message)
            : base(message)
        {
            // Intended blank
        }

        public ExtendableException(String message, System.Exception cause)
            : base(message, cause)
	    {
            // Intended blank
	    }
    }
}