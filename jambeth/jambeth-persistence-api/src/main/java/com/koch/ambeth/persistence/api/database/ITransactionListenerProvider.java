package com.koch.ambeth.persistence.api.database;

public interface ITransactionListenerProvider
{
	ITransactionListener[] getTransactionListeners();
}
