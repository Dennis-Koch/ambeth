using System;
using System.Windows;
using System.Windows.Controls;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Cache;
using System.Reflection;
using System.Threading;
using De.Osthus.Minerva.Core;
using System.ComponentModel;
#if !SILVERLIGHT
using System.Windows.Forms;
#endif

namespace De.Osthus.Minerva.View
{
    public partial class MainPage : UserControl, IInitializingBean
    {
        #region injected members
        public IServiceContext BeanContext { get; set; }

        public SynchronizationContext SyncContext { get; set; }
        #endregion

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(SyncContext, "SyncContext");
        }
    }
}
