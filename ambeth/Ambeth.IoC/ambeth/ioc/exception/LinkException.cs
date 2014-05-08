using De.Osthus.Ambeth.Ioc.Link;
using System;

namespace De.Osthus.Ambeth.Ioc.Exceptions
{
    public class LinkException : System.Exception
    {
        protected readonly AbstractLinkContainer linkContainer;

        public LinkException(String message, System.Exception cause, AbstractLinkContainer linkContainer)
            : base(message, cause)
        {
            this.linkContainer = linkContainer;
        }

        public LinkException(String message, AbstractLinkContainer linkContainer)
            : base(message)
        {
            this.linkContainer = linkContainer;
        }
    }
}