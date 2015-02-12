package de.osthus.ambeth.database;

import de.osthus.ambeth.merge.ILightweightTransaction;

public interface ITransaction extends ILightweightTransaction
{
	ITransactionInfo getTransactionInfo();

	void begin(boolean readOnly);

	void commit();

	void rollback(boolean fatalError);

	void processAndCommit(DatabaseCallback databaseCallback);

	void processAndCommit(DatabaseCallback databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly);

	<R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback);

	<R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly);
}
