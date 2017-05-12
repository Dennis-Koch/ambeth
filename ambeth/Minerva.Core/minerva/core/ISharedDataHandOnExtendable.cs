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
using System.Reflection;
using De.Osthus.Minerva.Extendable;
using De.Osthus.Ambeth.Ioc;
using System.Collections.Generic;

namespace De.Osthus.Minerva.Core
{
    public interface ISharedDataHandOnExtendable
    {
        void RegisterSharedDataHandOn(ISharedDataHandOn sharedDataHandOn, String token);

        void UnregisterSharedDataHandOn(ISharedDataHandOn sharedDataHandOn, String token);
    }
}
