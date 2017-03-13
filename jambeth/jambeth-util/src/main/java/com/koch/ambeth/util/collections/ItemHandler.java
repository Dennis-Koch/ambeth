package com.koch.ambeth.util.collections;

public abstract class ItemHandler<V>
{
	public ItemHandler()
	{

	}

	public abstract void handleItem(final V item);
}
