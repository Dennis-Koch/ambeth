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
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Minerva.Security
{
    public interface ISecurityScopeChangeListener
    {
        void SecurityScopeChanged(ISecurityScope[] securityScope);
    }
}
