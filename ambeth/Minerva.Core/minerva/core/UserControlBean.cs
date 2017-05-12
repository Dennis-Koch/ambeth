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
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Bind
{
    public class UserControlBean : UserControl, IInitializingBean, IDisposableBean, IDisposable
    {
        public IServiceContext BeanContext { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
        }

        public virtual void Destroy()
        {
            // Intended blank
        }

        public void Dispose() // Intended not to be overrideable
        {
            if (BeanContext != null)
            {
                BeanContext.Dispose();
                BeanContext = null;
            }
        }
    }
}
