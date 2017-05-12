using System;
using System.Reflection;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace Telerik.Windows.Controls
{
    public class WindowBaseEvents : HeaderedContentControlEvents
    {
        static WindowBaseEvents()
        {
            EventsUtil.initFields(typeof(WindowBaseEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<EventHandler> Activated { get; private set; }

        public static IEventDelegate<EventHandler> LayoutChangeEnded { get; private set; }

        public static IEventDelegate<EventHandler> LayoutChangeStarted { get; private set; }

        public static IEventDelegate<EventHandler> LocationChanged { get; private set; }

        public static IEventDelegate<EventHandler> WindowStateChanged { get; private set; }
    }
}
