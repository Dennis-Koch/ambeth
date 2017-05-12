using System;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class BeanAlreadyDisposedException : Exception
    {
        public BeanAlreadyDisposedException(String message)
            : base(message)
        {
            // intended blank
        }
    }
}
