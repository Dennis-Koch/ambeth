package de.osthus.ambeth.database;


public interface ITransactionInfo
{
	long getSessionId();

	boolean isReadOnly();
}
