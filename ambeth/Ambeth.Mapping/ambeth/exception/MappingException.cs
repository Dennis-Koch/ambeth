using System;

namespace De.Osthus.Ambeth.Exceptions
{
    public class MappingException : Exception
    {
	    public MappingException()
	    {
            // Intended blank
	    }

	    public MappingException(String message) : base(message)
	    {
            // Intended blank
        }

	    public MappingException(Exception cause) : base("MappingException", cause)
	    {
            // Intended blank
        }

        public MappingException(String message, Exception cause) : base(message, cause)
	    {
            // Intended blank
	    }
    }
}