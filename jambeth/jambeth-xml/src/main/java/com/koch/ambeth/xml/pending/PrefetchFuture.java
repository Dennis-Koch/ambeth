package com.koch.ambeth.xml.pending;

public class PrefetchFuture implements IObjectFuture
{
	private Iterable<Object> toPrefetch;

	public PrefetchFuture(Iterable<Object> toPrefetch)
	{
		this.toPrefetch = toPrefetch;
	}

	@Override
	public Object getValue()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public Iterable<Object> getToPrefetch()
	{
		return toPrefetch;
	}
}
