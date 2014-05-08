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
using De.Osthus.Ambeth.Ioc.Hierarchy;

namespace De.Osthus.Minerva.Dialogs
{
    public interface IWindowFactory
    {
        IBeanContextHolder<RadWindow> CreateDefaultWindow();

        IBeanContextHolder<RadWindow> CreateDefaultWindow(String header, UserControl content);
    }
}
