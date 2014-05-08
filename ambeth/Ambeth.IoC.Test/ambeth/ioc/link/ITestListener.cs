using System;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ITestListener
    {
        void MyMethod(Object sender, PropertyChangedEventArgs e);
    }
}
