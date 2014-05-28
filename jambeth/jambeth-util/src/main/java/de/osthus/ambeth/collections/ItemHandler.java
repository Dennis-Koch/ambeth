package de.osthus.ambeth.collections;

public abstract class ItemHandler<V>
{
	public ItemHandler()
	{

	}

	public abstract void handleItem(final V item);
}
