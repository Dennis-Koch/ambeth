package de.osthus.ambeth.objectcollector;

import de.osthus.ambeth.util.IDisposable;

public abstract class Collectable implements ICollectable, IDisposable, IObjectCollectorAware
{
	transient protected boolean disposed = true;

	protected transient IObjectCollector objectCollector;

	@Override
	public void setObjectCollector(IObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public void initInternDoNotCall()
	{
		if (!disposed)
		{
			throw new IllegalStateException("Object was not disposed " + getClass().toString());
		}
		disposed = false;
	}

	@Override
	public void dispose()
	{
		if (objectCollector != null)
		{
			objectCollector.dispose(this);
		}
	}

	@Override
	public void disposeInternDoNotCall()
	{
		if (disposed && objectCollector != null)
		{
			throw new IllegalStateException("Object already disposed " + getClass().toString());
		}
		disposed = true;
	}

	/**
	 * @return the disposed
	 */
	public final boolean isDisposed()
	{
		return disposed;
	}

	/**
	 * @param disposed
	 *            the disposed to set
	 */
	protected final void setDisposed(boolean disposed)
	{
		this.disposed = disposed;
	}
}
