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
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Dialogs
{
    public class DialogProvider : IDialogProvider, IInitializingBean
    {
        public IWindowFactory WindowFactory { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(WindowFactory, "WindowFactory");
        }

        public void ShowErrorDialog(String message, Exception exception)
        {
            
            //WindowFactory.CreateDefaultWindow(message, content);
        }
    }
}
