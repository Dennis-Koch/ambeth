using System.Threading;

namespace De.Osthus.Ambeth.Threading
{
    public class UIThreadWrapper
    {
        public Thread UIThread { get; private set; }

        public UIThreadWrapper(Thread uiThread)
        {
            UIThread = uiThread;
        }
    }
}