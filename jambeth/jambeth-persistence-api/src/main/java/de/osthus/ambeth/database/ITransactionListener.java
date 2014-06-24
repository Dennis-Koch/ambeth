package de.osthus.ambeth.database;

public interface ITransactionListener
{
	void handlePreCommit();
}
