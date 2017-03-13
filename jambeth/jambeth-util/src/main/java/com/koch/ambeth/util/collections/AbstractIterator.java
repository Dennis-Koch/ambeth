package com.koch.ambeth.util.collections;

import java.util.Iterator;

public abstract class AbstractIterator<V> implements Iterator<V>
{
	protected final boolean removeAllowed;

	public AbstractIterator()
	{
		this(false);
	}

	public AbstractIterator(boolean removeAllowed)
	{
		this.removeAllowed = removeAllowed;
	}
}
