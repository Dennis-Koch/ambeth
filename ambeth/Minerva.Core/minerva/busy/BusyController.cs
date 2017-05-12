using System.ComponentModel;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Collections;
using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Busy
{
    public class BusyController : IBusyController, IInitializingBean
    {
        public readonly PropertyChangedEventArgs isBusyPCE = new PropertyChangedEventArgs("IsBusy");

        public readonly PropertyChangedEventArgs busyCountPCE = new PropertyChangedEventArgs("BusyCount");

        public event PropertyChangedEventHandler PropertyChanged;

        protected readonly IdentityHashSet<IBusyToken> pendingTokens = new IdentityHashSet<IBusyToken>();

        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
        }

        public bool IsBusy
        {
            get
            {
                return pendingTokens.Count > 0;
            }
        }

        public int BusyCount
        {
            get { return pendingTokens.Count; }
        }

        public void ExecuteBusy(IBackgroundWorkerDelegate busyDelegate)
        {
            IBusyToken token = AcquireBusyToken();
            try
            {
                busyDelegate.Invoke();
            }
            finally
            {
                token.Finished();
            }
        }

        public R ExecuteBusy<R>(IResultingBackgroundWorkerDelegate<R> busyDelegate)
        {
            IBusyToken token = AcquireBusyToken();
            try
            {
                return busyDelegate.Invoke();
            }
            finally
            {
                token.Finished();
            }
        }

        public IBusyToken AcquireBusyToken()
        {
            BusyToken token = new BusyToken(this);
            bool changed;
            lock (pendingTokens)
            {
                pendingTokens.Add(token);
                changed = (pendingTokens.Count == 1);
            }
            GuiThreadHelper.InvokeInGui(delegate()
            {
                PropertyChanged(this, busyCountPCE);
                // Busy flag might evaluate to true
                if (changed)
                {
                    PropertyChanged(this, isBusyPCE);
                }
            });
            return token;
        }

        public void Finished(IBusyToken busyToken)
        {
            bool changed;
            lock (pendingTokens)
            {
                if (!pendingTokens.Remove(busyToken))
                {
                    throw new ArgumentException("Token not known");
                }
                changed = (pendingTokens.Count == 0);
            }
            GuiThreadHelper.InvokeInGui(delegate()
            {
                PropertyChanged(this, busyCountPCE);
                // Busy flag might evaluate to false
                if (changed)
                {
                    PropertyChanged(this, isBusyPCE);
                }
            });
        }
    }
}
