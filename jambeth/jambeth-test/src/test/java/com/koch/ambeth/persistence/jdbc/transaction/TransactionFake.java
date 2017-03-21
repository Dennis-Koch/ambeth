package com.koch.ambeth.persistence.jdbc.transaction;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
