using De.Osthus.Ambeth.Ioc;
using System.Threading;
using System;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Threading
{
    public class GuiThreadHelper : IGuiThreadHelper
    {
        [Autowired(Optional = true)]
        public UIThreadWrapper UIThreadWrapper
        {
            set
            {
                uiThread = value.UIThread;
            }
        }

        [Autowired(Optional = true)]
        public SynchronizationContext SyncContext { protected get; set; }

        [Autowired(Optional = true)]
        public IThreadPool ThreadPool { protected get; set; }

        protected Thread uiThread = Thread.CurrentThread;

	    public bool IsInGuiThread()
	    {
            return SyncContext != null && Thread.CurrentThread.Equals(uiThread);
	    }

        public void InvokeInGuiAndWait(IBackgroundWorkerDelegate callback)
        {
            InvokeInGuiAndWait(delegate(Object state) { callback(); }, null);
        }

        public R InvokeInGuiAndWait<R>(IResultingBackgroundWorkerDelegate<R> callback)
        {
            if (IsInGuiThread() || SyncContext == null)
            {
                return callback();
            }
            R result = default(R);
            SyncContext.Send(delegate(Object state)
            {
                result = callback();
            }, null);
            return result;
        }

        public R InvokeInGuiAndWait<R, P>(IResultingBackgroundWorkerParamDelegate<R, P> callback, P state)
        {
            if (IsInGuiThread() || SyncContext == null)
            {
                return callback(state);
            }
            R result = default(R);
            SyncContext.Send(delegate(Object state2)
            {
                result = callback(state);
            }, null);
            return result;
        }

        public void InvokeInGuiAndWait(SendOrPostCallback callback, Object state)
        {
            if (IsInGuiThread() || SyncContext == null)
            {
                callback(state);
                return;
            }
            SyncContext.Send(callback, state);
        }

        public void InvokeInGuiLate(IBackgroundWorkerDelegate callback)
        {
            InvokeInGuiLate(delegate(Object state) { callback(); }, null);
        }

        public void InvokeInGui(IBackgroundWorkerDelegate callback)
        {
            InvokeInGui(delegate(Object state) { callback(); }, null);
        }

        public void InvokeInGui(SendOrPostCallback callback, Object state)
        {
            if (IsInGuiThread() || SyncContext == null)
            {
                callback(state);
                return;
            }
            SyncContext.Post(callback, state);
        }

        public void InvokeInGuiLate(SendOrPostCallback callback, Object state)
        {
            if (SyncContext == null)
            {
                callback(state);
                return;
            }
            SyncContext.Post(callback, state);
        }

        public void InvokeOutOfGui(IBackgroundWorkerDelegate callback)
        {
            if (ThreadPool != null && IsInGuiThread())
            {
                ThreadPool.Queue(callback);
                return;
            }
            callback();
        }
    }
}