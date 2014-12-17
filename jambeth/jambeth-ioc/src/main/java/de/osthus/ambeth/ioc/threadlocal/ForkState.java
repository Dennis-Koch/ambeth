package de.osthus.ambeth.ioc.threadlocal;

public class ForkState implements IForkState
{
	protected final ForkStateEntry[] forkStateEntries;

	protected final IForkedValueResolver[] forkedValueResolvers;

	public ForkState(ForkStateEntry[] forkStateEntries, IForkedValueResolver[] forkedValueResolvers)
	{
		this.forkStateEntries = forkStateEntries;
		this.forkedValueResolvers = forkedValueResolvers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void use(Runnable runnable)
	{
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;

		Object[] oldValues = new Object[forkedValueResolvers.length];
		for (int a = 0, size = forkStateEntries.length; a < size; a++)
		{
			ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			oldValues[a] = tlHandle.get();
			Object forkedValue = forkedValueResolvers[a].getForkedValue();
			tlHandle.set(forkedValue);
		}
		try
		{
			runnable.run();
		}
		finally
		{
			for (int a = 0, size = forkStateEntries.length; a < size; a++)
			{
				ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
				Object oldValue = oldValues[a];
				if (oldValue == null)
				{
					tlHandle.remove();
				}
				else
				{
					tlHandle.set(oldValue);
				}
			}
		}
	}
}
