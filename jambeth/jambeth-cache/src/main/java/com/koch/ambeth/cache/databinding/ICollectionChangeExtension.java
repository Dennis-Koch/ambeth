package com.koch.ambeth.cache.databinding;

import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedEvent;

public interface ICollectionChangeExtension
{
	void collectionChanged(Object obj, NotifyCollectionChangedEvent evnt);
}