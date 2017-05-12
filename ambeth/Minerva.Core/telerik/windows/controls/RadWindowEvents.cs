using System;
using System.Reflection;
using System.Windows;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace Telerik.Windows.Controls
{
    public class RadWindowEvents : WindowBaseEvents
    {
        static RadWindowEvents()
        {
            EventsUtil.initFields(typeof(RadWindowEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<RoutedEventHandler> Opened { get; private set; }

        public static IEventDelegate<EventHandler<WindowClosedEventArgs>> Closed { get; private set; }

        public static IEventDelegate<EventHandler<WindowPreviewClosedEventArgs>> PreviewClosed { get; private set; }
    }
}
