package de.osthus.ambeth.databinding;

import de.osthus.ambeth.collections.specialized.NotifyCollectionChangedEvent;

public interface ICollectionChangeExtension
{
	void collectionChanged(Object obj, NotifyCollectionChangedEvent evnt);
}