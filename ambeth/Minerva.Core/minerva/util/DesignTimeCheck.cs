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
using System.ComponentModel;

namespace De.Osthus.Minerva.Bind
{
    public class DesignTimeCheck
    {
        public static bool IsInDesignMode
        {
            get
            {
#if !SILVERLIGHT
//            return !HtmlPage.IsEnabled;
             DependencyProperty prop = DesignerProperties.IsInDesignModeProperty;
             return (bool)DependencyPropertyDescriptor
                    .FromProperty(prop, typeof(FrameworkElement))
                    .Metadata.DefaultValue;
#else
                return DesignerProperties.IsInDesignTool;
#endif
            }
        }
    }
}
