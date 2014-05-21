using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
#if SILVERLIGHT
using De.Osthus.Ambeth.Util;
#endif

namespace De.Osthus.Ambeth.Cache
{
    public class CacheModification : ICacheModification
    {
        private const int NOT_ACTIVE = 0, ACTIVE = 1, FLUSHING = 2;

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly ThreadLocal<int> activeTL = new ThreadLocal<int>();

        protected readonly ThreadLocal<bool> internalUpdateTL = new ThreadLocal<bool>();

        protected readonly ThreadLocal<List<IBackgroundWorkerDelegate>> queuedEventsTL = new ThreadLocal<List<IBackgroundWorkerDelegate>>();

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        public bool ActiveOrFlushing
        {
            get
            {
                return activeTL.Value != NOT_ACTIVE;
            }
        }

        public bool Active
        {
            get
            {
                return activeTL.Value == ACTIVE;
            }
            set
            {
                bool existingIsActive = Active;
                if (existingIsActive == value)
                {
                    return;
                }
                if (existingIsActive)
                {
                    activeTL.Value = FLUSHING;
                    try
                    {
                        FireQueuedPropertyChangeEvents();
                    }
                    finally
                    {
                        activeTL.Value = NOT_ACTIVE;
                    }
                }
                else
                {
                    activeTL.Value = ACTIVE;
                }
            }
        }

        public void QueuePropertyChangeEvent(IBackgroundWorkerDelegate task)
        {
            if (!Active)
            {
                throw new Exception("Not supported if IsActive is 'false'");
            }
            List<IBackgroundWorkerDelegate> queuedEvents = queuedEventsTL.Value;
            if (queuedEvents == null)
            {
                queuedEvents = new List<IBackgroundWorkerDelegate>();
                queuedEventsTL.Value = queuedEvents;
            }
            queuedEvents.Add(task);
        }

        protected void FireQueuedPropertyChangeEvents()
        {
            List<IBackgroundWorkerDelegate> queuedEvents = queuedEventsTL.Value;
            if (queuedEvents == null)
            {
                return;
            }
            queuedEventsTL.Value = null;
            Log.Info("ICacheModification.FlushInGui()");
            GuiThreadHelper.InvokeInGui(delegate()
            {
                Log.Info("ICacheModification.FlushWithinGui()");
                try
                {
                    for (int a = 0, size = queuedEvents.Count; a < size; a++)
                    {
                        IBackgroundWorkerDelegate queuedEvent = queuedEvents[a];
                        queuedEvent();
                    }
                }
                catch (Exception e)
                {
                    Log.Error(e);
                    throw;
                }
                Log.Info("ICacheModification.FlushWithinGui finished");
            });
        }

        public bool InternalUpdate
        {
            get
            {
                return internalUpdateTL.Value;
            }
            set
            {
                internalUpdateTL.Value = value;
            }
        }
    }
}
