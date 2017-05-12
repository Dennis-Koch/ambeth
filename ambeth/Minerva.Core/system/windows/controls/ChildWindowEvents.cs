using System;
using System.Reflection;
using De.Osthus.Minerva.Bind;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc.Link;

namespace System.Windows.Controls
{
    public class ChildWindowEvents : ContentControlEvents
    {
        static ChildWindowEvents()
        {
            EventsUtil.initFields(typeof(ChildWindowEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<EventHandler> Closed { get; private set; }

        public static IEventDelegate<EventHandler<CancelEventArgs>> Closing { get; private set; }
    }
}
