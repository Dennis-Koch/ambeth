namespace De.Osthus.Ambeth.Cache
{
    public interface ITransactionalRootCache
    {
        void AcquireTransactionalRootCache();

        void DisposeTransactionalRootCache(bool success);
    }
}