package com.koch.ambeth.util.collections.specialized;

/**
 * Describes the action that caused a INotifyCollectionChanged.CollectionChanged event.
 */
public enum NotifyCollectionChangedAction
{
	/** One or more items were added to the collection. */
	Add(0),

	/** One or more items were removed from the collection. */
	Remove(1),

	/** One or more items were replaced in the collection. */
	Replace(2),

	/** One or more items were moved within the collection. */
	Move(3),

	/** The content of the collection changed dramatically. */
	Reset(4);

	private final int value;

	NotifyCollectionChangedAction(int value)
	{
		this.value = value;
	}

	public int value()
	{
		return value;
	}
}
