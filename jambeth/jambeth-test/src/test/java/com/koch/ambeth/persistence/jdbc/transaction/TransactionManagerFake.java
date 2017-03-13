package com.koch.ambeth.persistence.jdbc.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class TransactionManagerFake implements TransactionManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private Transaction transaction;

	@Override
	public void begin() throws NotSupportedException, SystemException
	{
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException,
			SystemException
	{
	}

	@Override
	public int getStatus() throws SystemException
	{
		return 0;
	}

	@Override
	public Transaction getTransaction() throws SystemException
	{
		if (transaction == null)
		{
			transaction = new TransactionFake();
		}
		return transaction;
	}

	@Override
	public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException
	{
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException
	{
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException
	{
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException
	{
	}

	@Override
	public Transaction suspend() throws SystemException
	{
		return null;
	}
}
