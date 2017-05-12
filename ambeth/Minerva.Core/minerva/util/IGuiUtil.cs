using System;
using System.Collections.Generic;

#if SILVERLIGHT
using System.Windows.Controls;
#endif

namespace De.Osthus.Minerva.Bind
{
    public interface IGuiUtil
    {
#if SILVERLIGHT
        Control GetControlByName(IList<Control> ctrls, String ctrlName);
#endif

        void ShowErrorDialog(String errorMessage);
    }
}
