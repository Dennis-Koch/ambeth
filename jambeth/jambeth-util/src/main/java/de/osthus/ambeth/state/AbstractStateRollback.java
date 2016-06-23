package de.osthus.ambeth.state;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public abstract class AbstractStateRollback implements IStateRollback
{
	private final IStateRollback[] rollbacks;

	private boolean rollbackCalled;

	public AbstractStateRollback(IStateRollback[] rollbacks)
	{
		this.rollbacks = rollbacks;
	}

	@Override
	public final void rollback()
	{
		if (rollbackCalled)
		{
			throw new IllegalStateException("rollback() has already been called");
		}
		rollbackCalled = true;
		try
		{
			rollbackIntern();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			for (int a = rollbacks.length; a-- > 0;)
			{
				IStateRollback rollback = rollbacks[a];
				if (rollback == null)
				{
					continue;
				}
				rollback.rollback();
			}
		}
	}

	protected abstract void rollbackIntern() throws Throwable;
}
