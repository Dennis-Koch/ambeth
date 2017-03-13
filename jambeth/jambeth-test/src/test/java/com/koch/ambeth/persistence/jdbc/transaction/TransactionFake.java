package com.koch.ambeth.persistence.jdbc.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class TransactionFake implements Transaction
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
	private boolean rollbackOnly;

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException,
			SystemException
	{
	}

	@Override
	public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException
	{
		return false;
	}

	@Override
	public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException
	{
		return false;
	}

	@Override
	public int getStatus() throws SystemException
	{
		return 0;
	}

	@Override
	public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException
	{
	}

	@Override
	public void rollback() throws IllegalStateException, SystemException
	{
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException
	{
		rollbackOnly = true;
	}

	public boolean getRollbackOnly()
	{
		return rollbackOnly;
	}
}
