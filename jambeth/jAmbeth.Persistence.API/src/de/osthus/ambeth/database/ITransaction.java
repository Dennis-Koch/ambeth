package de.osthus.ambeth.database;

public interface ITransaction
{
	boolean isActive();

	void begin(boolean readonly);

	void commit();

	void rollback(boolean fatalError);

	void processAndCommit(DatabaseCallback databaseCallback);

	void processAndCommit(DatabaseCallback databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly);

	<R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback);

	<R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly);
}
