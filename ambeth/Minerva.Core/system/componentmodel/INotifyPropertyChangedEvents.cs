using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Minerva.Bind;
using System.Reflection;

namespace System.ComponentModel
{
    public class INotifyPropertyChangedEvents
    {
        static INotifyPropertyChangedEvents()
        {
            EventsUtil.initFields(typeof(INotifyPropertyChangedEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<PropertyChangedEventHandler> PropertyChanged { get; private set; }
    }
}
