namespace De.Osthus.Ambeth.Database
{
    public interface ITransaction
    {
        bool Active { get; }

        void Begin(bool readOnly);

        void Commit();

        void Rollback(bool fatalError);

        void ProcessAndCommit(DatabaseCallback databaseCallback);

        void ProcessAndCommit(DatabaseCallback databaseCallback, bool expectOwnDatabaseSession, bool readOnly);

        R ProcessAndCommit<R>(ResultingDatabaseCallback<R> databaseCallback);

        R ProcessAndCommit<R>(ResultingDatabaseCallback<R> databaseCallback, bool expectOwnDatabaseSession, bool readOnly);
    }
}
