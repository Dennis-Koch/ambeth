using System.Collections.Specialized;
using System.ComponentModel;
using System.Reflection;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace System.Windows
{
    public class GenericViewModelEvents
    {
        static GenericViewModelEvents()
        {
            EventsUtil.initFields(typeof(GenericViewModelEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }
        
        public static IEventDelegate<PropertyChangedEventHandler> NotPersistedChanged { get; private set; }

        public static IEventDelegate<NotifyCollectionChangedEventHandler> CollectionChanged { get; private set; }
    }
}
