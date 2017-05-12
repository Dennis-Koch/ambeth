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
using System.Dynamic;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Minerva.Core
{
    public class XamlBeanPropertyBinder
    {
        protected readonly String xamlBeanName;

        protected readonly String propertyToListenFor;

        protected readonly XamlBeanProvider xamlBeanProvider;

        public XamlBeanPropertyBinder(XamlBeanProvider xamlBeanProvider, String xamlBeanName, String propertyToListenFor)
        {
            this.xamlBeanProvider = xamlBeanProvider;
            this.xamlBeanName = xamlBeanName;
            this.propertyToListenFor = propertyToListenFor;
        }

        public void HandlePropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            if (!e.PropertyName.Equals(propertyToListenFor))
            {
                return;
            }
            xamlBeanProvider.OnPropertyChanged(xamlBeanName);
        }
    }
}
