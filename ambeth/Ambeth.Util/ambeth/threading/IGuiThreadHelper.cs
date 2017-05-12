using System.Threading;
using System;
namespace De.Osthus.Ambeth.Threading
{
    public interface IGuiThreadHelper
    {
        bool IsInGuiThread();

        void InvokeInGuiAndWait(IBackgroundWorkerDelegate callback);

        R InvokeInGuiAndWait<R>(IResultingBackgroundWorkerDelegate<R> callback);

        R InvokeInGuiAndWait<R, P>(IResultingBackgroundWorkerParamDelegate<R, P> callback, P state);

        void InvokeInGuiAndWait(SendOrPostCallback callback, Object state);

        void InvokeInGui(IBackgroundWorkerDelegate callback);

        void InvokeInGui(SendOrPostCallback callback, Object state);

        void InvokeInGuiLate(IBackgroundWorkerDelegate callback);

        void InvokeInGuiLate(SendOrPostCallback callback, Object state);

        void InvokeOutOfGui(IBackgroundWorkerDelegate callback);
    }
}