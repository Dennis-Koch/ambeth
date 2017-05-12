using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Telerik.Windows.Controls;
using De.Osthus.Ambeth.Config;
using System.Reflection;
using De.Osthus.Minerva.Bind;

namespace Telerik.Windows.Controls
{
    public class HeaderedContentControlEvents : ContentControlEvents
    {
        static HeaderedContentControlEvents()
        {
            EventsUtil.initFields(typeof(HeaderedContentControlEvents), delegate(FieldInfo field, Object eventDelegate)
            {
                field.SetValue(null, eventDelegate);
            });
        }
    }
}
