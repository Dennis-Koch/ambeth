package de.osthus.ambeth.merge;

public interface ITransactionState
{
	boolean isTransactionActive();

	Boolean isExternalTransactionManagerActive();

	void setExternalTransactionManagerActive(Boolean active);
}
