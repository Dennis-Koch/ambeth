package com.koch.ambeth.util.objectcollector;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.IDisposable;

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
