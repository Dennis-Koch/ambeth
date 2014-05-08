using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Exceptions
{
    [XmlType]
    public class EventPollException : System.Exception
    {
        public EventPollException(String message, System.Exception cause)
            : base(message, cause)
        {
            // Intended blank
        }

        public EventPollException(String message)
            : base(message)
        {
            // Intended blank
        }
    }
}
