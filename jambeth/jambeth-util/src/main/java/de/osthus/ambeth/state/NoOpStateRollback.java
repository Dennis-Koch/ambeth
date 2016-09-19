package de.osthus.ambeth.state;


public final class NoOpStateRollback implements IStateRollback
{
	public static final IStateRollback instance = new NoOpStateRollback();

	public static IStateRollback createNoOpRollback(IStateRollback[] rollbacks)
	{
		if (rollbacks == null || rollbacks.length == 0)
		{
			return instance;
		}
		if (rollbacks.length == 1)
		{
			return rollbacks[0];
		}
		return new AbstractStateRollback(rollbacks)
		{
			@Override
			protected void rollbackIntern() throws Throwable
			{
				// intended blank
			}
		};
	}

	private NoOpStateRollback()
	{
		// Intended blank
	}

	@Override
	public void rollback()
	{
		// intended blank
	}
}
