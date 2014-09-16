namespace De.Osthus.Ambeth.Persistence
{
    public interface IDatabaseDisposeHook
    {
        void DatabaseDisposed(IDatabase disposedDatabase);
    }
}