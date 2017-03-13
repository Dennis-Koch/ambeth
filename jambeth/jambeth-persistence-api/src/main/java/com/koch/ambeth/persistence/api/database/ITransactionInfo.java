package com.koch.ambeth.persistence.api.database;


public interface ITransactionInfo
{
	long getSessionId();

	boolean isReadOnly();
}
