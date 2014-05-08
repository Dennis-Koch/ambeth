using System;
using System.Reflection;
using De.Osthus.Minerva.Bind;

namespace System.Windows.Controls
{
    public class ContentControlEvents : ControlEvents
    {
        static ContentControlEvents()
        {
            EventsUtil.initFields(typeof(ContentControlEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }
    }
}
