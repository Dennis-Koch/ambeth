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
using System.Collections.Generic;
using System.Linq;
using Telerik.Windows.Controls;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Minerva.Dialogs;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Bind
{
    public class GuiUtil : IGuiUtil, IInitializingBean
    {
        public IWindowFactory WindowFactory { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(WindowFactory, "WindowFactory");
        }

        public Control GetControlByName(IList<Control> ctrls, string ctrlName)
        {
            if (ctrls == null || ctrls.Count == 0) return null;

            Control ctrl = (from c in ctrls
                            where c != null && c.Name == ctrlName
                            select c).FirstOrDefault();

            return ctrl;
        }

        public void ShowErrorDialog(string errorMessage)
        {
            WindowFactory.CreateDefaultWindow("Error occurs", new ErrorDialog());
            
            //TexisWindow errorDialog = new TexisWindow("Error occurs", new ErrorDialog(errorMessage));
            //errorDialog.ResizeMode = ResizeMode.NoResize;
            //errorDialog.ShowDialog();
        }
    }
}
