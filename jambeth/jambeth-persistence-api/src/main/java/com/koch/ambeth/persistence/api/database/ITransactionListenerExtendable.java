package com.koch.ambeth.persistence.api.database;

public interface ITransactionListenerExtendable
{
	void registerTransactionListener(ITransactionListener transactionListener);

	void unregisterTransactionListener(ITransactionListener transactionListener);
}
