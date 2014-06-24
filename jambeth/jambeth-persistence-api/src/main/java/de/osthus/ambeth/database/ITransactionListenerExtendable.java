package de.osthus.ambeth.database;

public interface ITransactionListenerExtendable
{
	void registerTransactionListener(ITransactionListener transactionListener);

	void unregisterTransactionListener(ITransactionListener transactionListener);
}
