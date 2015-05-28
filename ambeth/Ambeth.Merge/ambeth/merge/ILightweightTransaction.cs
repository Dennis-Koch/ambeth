using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Merge
{
    public interface ILightweightTransaction
    {
        bool Active { get; }

        void RunInTransaction(IBackgroundWorkerDelegate runnable);

        R RunInTransaction<R>(IResultingBackgroundWorkerDelegate<R> runnable);

        R RunInLazyTransaction<R>(IResultingBackgroundWorkerDelegate<R> runnable);

		void RunOnTransactionPreCommit(IBackgroundWorkerDelegate runnable);
    }
}