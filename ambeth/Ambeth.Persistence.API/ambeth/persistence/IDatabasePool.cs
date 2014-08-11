namespace De.Osthus.Ambeth.Persistence
{
    public interface IDatabasePool
    {
        IDatabase AcquireDatabase();

        IDatabase AcquireDatabase(bool readonlyMode);

        IDatabase TryAcquireDatabase();

        IDatabase TryAcquireDatabase(bool readonlyMode);

        void ReleaseDatabase(IDatabase database);

        void ReleaseDatabase(IDatabase database, bool backToPool);

        void Shutdown();
    }
}
