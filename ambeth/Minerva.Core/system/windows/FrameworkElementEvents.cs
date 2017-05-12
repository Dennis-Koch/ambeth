using System;
using System.Reflection;
using System.Windows.Controls;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace System.Windows
{
    public class FrameworkElementEvents : UIElementEvents
    {
        static FrameworkElementEvents()
        {
            EventsUtil.initFields(typeof(FrameworkElementEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<EventHandler<ValidationErrorEventArgs>> BindingValidationError { get; private set; }

        public static IEventDelegate<EventHandler> LayoutUpdated { get; private set; }

        public static IEventDelegate<RoutedEventHandler> Loaded { get; private set; }

        public static IEventDelegate<SizeChangedEventHandler> SizeChanged { get; private set; }

        public static IEventDelegate<RoutedEventHandler> Unloaded { get; private set; }
    }
}
