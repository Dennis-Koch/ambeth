package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.state.IStateRollback;
import de.osthus.ambeth.util.IDisposable;

/**
 * Interface of the jAmbeth database wrapper. Used for database related methods and accessing the database meta data via jAmbeth.
 * 
 * @author dennis.koch
 */
public interface IDatabase extends IDisposable, IDatabaseDisposeHookExtendable
{
	IDatabaseMetaData getMetaData();

	/**
	 * Returns the dispose state of the current instance
	 * 
	 * @return true if the IDatabase instance is not valid any more
	 */
	boolean isDisposed();

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
	<T> T getAutowiredBeanInContext(Class<T> autowiredType);

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
	<T> T getNamedBeanInContext(String beanName, Class<T> expectedType);

	/**
	 * Getter for the random Ambeth database session ID. It identifies the instance in the DefaultDatabasePool.
	 * 
	 * @return Session ID.
	 */
	long getSessionId();

	/**
	 * Setter for the random Ambeth database session ID. It identifies the instance in the DefaultDatabasePool.
	 * 
	 * @param sessionId
	 */
	void setSessionId(long sessionId);

	/**
	 * Getter for the context provider of this database representation.
	 * 
	 * @return Context provider.
	 */
	IContextProvider getContextProvider();

	/**
	 * Getter for the database pool this instance is managed by.
	 * 
	 * @return Database pool.
	 */
	IDatabasePool getPool();

	/**
	 * Releases this instance after flushing the used database instance.
	 */
	void flushAndRelease();

	/**
	 * Releases this instance.
	 */
	void release(boolean errorOccured);

	/**
	 * Ambeth system method. Called after instantiation.
	 */
	void acquired(boolean readOnly);

	/**
	 * Getter for the thread local current instance of IDatabase. (This differs from "this" in cases where the IDatabase handle is only a proxy to a thread
	 * local instance. Do not call getCurrent() and pass that result to foreign code if you do not know what you do.
	 * 
	 * @return the thread-local instance of IDatabase
	 */
	IDatabase getCurrent();

	/**
	 * 
	 * @return Name of this database.
	 */
	String getName();

	/**
	 * 
	 * @return Names of schemas in use.
	 */
	String[] getSchemaNames();

	/**
	 * Getter for alle tables in this database.
	 * 
	 * @return List of all tables in this database.
	 */
	List<ITable> getTables();

	/**
	 * Getter for all defined relations in this database.
	 * 
	 * @return List of all relations defined for the content of this database.
	 */
	List<ILink> getLinks();

	/**
	 * Getter for a table identified by the persisted entity type.
	 * 
	 * @param entityType
	 *            Identifying type.
	 * @return Table used for persisting the given entity type.
	 */
	ITable getTableByType(Class<?> entityType);

	/**
	 * Getter for an archive table identified by the persisted entity type.
	 * 
	 * @param entityType
	 *            Identifying type.
	 * @return Table used for archiving the given entity type.
	 */
	ITable getArchiveTableByType(Class<?> entityType);

	/**
	 * Getter for a table identified by name.
	 * 
	 * @param tableName
	 *            Identifying name.
	 * @return Table of given name.
	 */
	ITable getTableByName(String tableName);

	/**
	 * 
	 * @param table1
	 * @param table2
	 * @return Links connection the two tables.
	 */
	List<ILink> getLinksByTables(ITable table1, ITable table2);

	/**
	 * Checks the status of the database connection.
	 * 
	 * @return True if the connection can be used, otherwise false.
	 */
	boolean test();

	/**
	 * Commits all pending changes to storage.
	 */
	void flush();

	/**
	 * Reverts all pending changes.
	 */
	void revert();

	/**
	 * Reverts all pending changes down to the savepoint.
	 * 
	 * @param savepoint
	 *            savepoint that defines the changes to revert.
	 */
	void revert(ISavepoint savepoint);

	/**
	 * Acquires a savepoint that may be used later for reverting changes.
	 * 
	 * @return savepoint created
	 */
	ISavepoint setSavepoint();

	/**
	 * Releases the savepoint
	 * 
	 * @param savepoint
	 *            savepoint to release.
	 */
	void releaseSavepoint(ISavepoint savepoint);

	/**
	 * Transaction rollback.
	 */
	void rollback(ISavepoint savepoint);

	/**
	 * Delay foreign key constraint validation for running multiple commands "as one".
	 * 
	 * @return The delegate to revert the disabling of constraints
	 */
	IStateRollback disableConstraints();

	/**
	 * Register a new table after the context was started
	 * 
	 * @param tableName
	 */
	void registerNewTable(String tableName);
}
