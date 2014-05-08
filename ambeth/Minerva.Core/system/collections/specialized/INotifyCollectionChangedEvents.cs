using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Minerva.Bind;
using System.Collections.Specialized;
using System.Reflection;

namespace System.ComponentModel
{
    public class INotifyCollectionChangedEvents
    {
        static INotifyCollectionChangedEvents()
        {
            EventsUtil.initFields(typeof(INotifyCollectionChangedEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<NotifyCollectionChangedEventHandler> CollectionChanged { get; private set; }
    }
}
