using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Util
{
    public interface IMultithreadingHelper
    {
        void InvokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount);

        void InvokeInParallel(IServiceContext serviceContext, params Runnable[] runnables);

      	void InvokeInParallel(IServiceContext serviceContext, bool inheritThreadLocals, Runnable runnable, int workerCount);

    	void InvokeInParallel(IServiceContext serviceContext, bool inheritThreadLocals, params Runnable[] runnables);
    }
}