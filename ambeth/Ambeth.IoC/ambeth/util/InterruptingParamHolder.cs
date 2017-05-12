using System;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
#if !SILVERLIGHT
    public class InterruptingParamHolder : ParamHolder<Exception>
    {
        protected readonly Thread mainThread;

        public InterruptingParamHolder(Thread mainThread)
        {
            this.mainThread = mainThread;
        }

        public override Exception Value
        {
            get
            {
                return base.Value;
            }
            set
            {
                base.Value = value;
                if (value != null)
                {
                    // necessary to inform the main thread that it should not wait any longer for the latch
                    mainThread.Interrupt();
                }
            }
        }
    }
#endif
}