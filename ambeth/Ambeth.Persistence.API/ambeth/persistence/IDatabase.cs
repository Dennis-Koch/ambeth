using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    /**
     * Interface of the jAmbeth database wrapper. Used for database related methods and accessing the database meta data via jAmbeth.
     * 
     * @author dennis.koch
     */
    public interface IDatabase : IDisposable, IDatabaseDisposeHookExtendable
    {
        IDatabaseMetaData MetaData { get; }

        /**
         * Returns the dispose state of the current instance
         * 
         * @return true if the IDatabase instance is not valid any more
         */
        bool Disposed { get; }

        /**
         * Allows to retrieve beans which are 1:1 related to the underlying database artifact. One example may be a java.sql.Connection bean for JDBC related
         * database handles
         * 
         * @param <T>
         *            May be anything
         * @param autowiredType
         *            The type where a bean is supposed to be autowired to
         * @return null, if no bean has been found
         * @return Instance of T if bean has been found
         * 
         */
        T GetAutowiredBeanInContext<T>();

        /**
         * 
         * Allows to retrieve beans which are 1:1 related to the underlying database artifact. One example may be a java.sql.Connection bean for JDBC related
         * database handles
         * 
         * @param <T>
         *            May be anything
         * @param beanName
         *            The name where a bean is supposed to be uniquely bound to
         * @param expectedType
         *            The expected type to skip later casts
         * @return null, if no bean has been found
         * @return Instance of T if bean has been found
         * @throws ClassCastException
         *             if bean has been found but is not assignable to type <T>
         */
        T GetNamedBeanInContext<T>(String beanName);

        /**
         * The random Ambeth database session ID. It identifies the instance in the DefaultDatabasePool.
         * 
         * @return Session ID.
         */
        long SessionId { get; set; }

        /**
         * Getter for the context provider of this database representation.
         * 
         * @return Context provider.
         */
        IContextProvider GetContextProvider();
        
        /**
         * Releases this instance after flushing the used database instance.
         */
        void FlushAndRelease();

        /**
         * Releases this instance.
         */
        void Release(bool errorOccured);

        /**
         * Ambeth system method. Called after instantiation.
         */
        void Acquired(bool readOnly);

        /**
         * Getter for the thread local current instance of IDatabase. (This differs from "this" in cases where the IDatabase handle is only a proxy to a thread
         * local instance. Do not call getCurrent() and pass that result to foreign code if you do not know what you do.
         * 
         * @return the thread-local instance of IDatabase
         */
        IDatabase GetCurrent();
        
        /**
         * Getter for alle tables in this database.
         * 
         * @return List of all tables in this database.
         */
        IList<ITable> GetTables();

        /**
         * Getter for all defined relations in this database.
         * 
         * @return List of all relations defined for the content of this database.
         */
        IList<ILink> GetLinks();

        /**
         * Getter for a table identified by the persisted entity type.
         * 
         * @param entityType
         *            Identifying type.
         * @return Table used for persisting the given entity type.
         */
        ITable GetTableByType(Type entityType);

        /**
         * Getter for an archive table identified by the persisted entity type.
         * 
         * @param entityType
         *            Identifying type.
         * @return Table used for archiving the given entity type.
         */
        ITable GetArchiveTableByType(Type entityType);
        
        /**
         * Getter for a table identified by name.
         * 
         * @param tableName
         *            Identifying name.
         * @return Table of given name.
         */
        ITable GetTableByName(String tableName);
        
        /**
         * 
         * @param table1
         * @param table2
         * @return Links connection the two tables.
         */
        IList<ILink> GetLinksByTables(ITable table1, ITable table2);

        /**
         * Checks the status of the database connection.
         * 
         * @return True if the connection can be used, otherwise false.
         */
        bool Test();

        /**
         * Commits all pending changes to storage.
         */
        void Flush();

        /**
         * Reverts all pending changes.
         */
        void Revert();

        /**
         * Reverts all pending changes down to the savepoint.
         * 
         * @param savepoint
         *            savepoint that defines the changes to revert.
         */
        void Revert(ISavepoint savepoint);

        /**
         * Acquires a savepoint that may be used later for reverting changes.
         * 
         * @return savepoint created
         */
        ISavepoint SetSavepoint();

        /**
         * Releases the savepoint
         * 
         * @param savepoint
         *            savepoint to release.
         */
        void ReleaseSavepoint(ISavepoint savepoint);

        /**
         * Transaction rollback.
         */
        void Rollback(ISavepoint savepoint);

        /**
         * Delay foreign key constraint validation for running multiple commands "as one".
         * 
         * @return List of names of disabled constraint
         */
        IList<String[]> DisableConstraints();

        /**
         * Re-enable the constraints.
         * 
         * @param disabled
         *            List of names of disabled constraint
         */
        void EnableConstraints(IList<String[]> disabled);
    }
}