using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Telerik.Windows.Controls;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Dialogs
{
    public partial class ErrorDialog : UserControl, IInitializingBean
    {
        public RadWindow ParentWindow { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ParentWindow, "ParentWindow");
        }
        
        #region event handler functions
        public void btnOK_Click(object sender, RoutedEventArgs e)
        {
            ParentWindow.Close();
        }
        #endregion
    }
}
