using System;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class TempStacktraceException : Exception
    {
        public TempStacktraceException() : base("Please configure your IDE to not capture this intended exception")
	    {
            // Intended blank
	    }
    }
}