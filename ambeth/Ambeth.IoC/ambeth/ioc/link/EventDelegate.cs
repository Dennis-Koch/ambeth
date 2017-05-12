using System;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class EventDelegate<T> : IEventDelegate<T> where T : class
    {
        protected String eventName;
        
        public EventDelegate(String eventName)
        {
            this.eventName = eventName;
        }

        public String EventName
        {
            get
            {
                return eventName;
            }
        }
    }
}
