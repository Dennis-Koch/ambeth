using De.Osthus.Ambeth.Log;
using System.ComponentModel;
using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class TestListener : ITestListener
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public void HandlePropertyChangedTest(Object sender, PropertyChangedEventArgs e)
        {
            LinkContainerTest.propertyChangedReceivedCount++;
        }

        public void MyMethod(Object sender, PropertyChangedEventArgs e)
        {
            LinkContainerTest.listenerReceivedCount++;
        }
    }
}
