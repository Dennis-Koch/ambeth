using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Minerva.Bind;
using System.Reflection;
using System.Windows.Input;

namespace System.Windows.Controls
{
    public class TextBoxEvents
    {
        static TextBoxEvents()
        {
            EventsUtil.initFields(typeof(TextBoxEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }

        public static IEventDelegate<TextChangedEventHandler> TextChanged { get; private set; }
        public static IEventDelegate<KeyEventHandler> KeyDown { get; private set; }
    }
}
