using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class TestRegistry : ITestListenerExtendable, IInitializingBean, ITestRegistry, INotifyPropertyChanged, IStartingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly IExtendableContainer<PropertyChangedEventHandler> propertyChangedList = new DefaultExtendableContainer<PropertyChangedEventHandler>("pceListener");

        public event PropertyChangedEventHandler PropertyChanged
        {
            add
            {
                propertyChangedList.Register(value);
            }
            remove
            {
                propertyChangedList.Unregister(value);
            }
        }

        protected readonly IExtendableContainer<ITestListener> testListeners = new DefaultExtendableContainer<ITestListener>("testListener");

        public void AfterPropertiesSet()
        {
        }

        public void AfterStarted()
        {
            PropertyChangedEventArgs pceArgs = new PropertyChangedEventArgs("Test");
            PropertyChangedEventHandler[] pceListeners = propertyChangedList.GetExtensions();
            foreach (PropertyChangedEventHandler pceListener in pceListeners)
            {
        	    pceListener.Invoke(this, pceArgs);
            }
            ITestListener[] testListeners = this.testListeners.GetExtensions();
            foreach (ITestListener testListener in testListeners)
            {
        	    testListener.MyMethod(this, pceArgs);
            }
        }

        public PropertyChangedEventHandler[] GetPceListeners()
        {
            return propertyChangedList.GetExtensions();
        }

        public ITestListener[] GetTestListeners()
        {
            return testListeners.GetExtensions();
        }

        public void RegisterTestListener(ITestListener testListener)
        {
            testListeners.Register(testListener);
        }

        public void UnregisterTestListener(ITestListener testListener)
        {
            testListeners.Unregister(testListener);
        }
        
    }
}
