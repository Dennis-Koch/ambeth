using System;
using System.Reflection;
using De.Osthus.Minerva.Bind;
using De.Osthus.Ambeth.Ioc.Link;

namespace System.Windows.Controls
{
    public class ControlEvents : FrameworkElementEvents
    {
        static ControlEvents()
        {
            EventsUtil.initFields(typeof(ControlEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<DependencyPropertyChangedEventHandler> IsEnabledChanged { get; private set; }
    }
}
