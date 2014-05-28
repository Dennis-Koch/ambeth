package de.osthus.ambeth.collections.specialized;

/**
 * Provides data for the {@link INotifyCollectionChanged} collectionChanged event
 */
public class NotifyCollectionChangedEvent extends java.util.EventObject
{
	private static final long serialVersionUID = 8307930854351655114L;

	protected final NotifyCollectionChangedAction action;

	protected final Object[] newItems;

	protected final Object[] oldItems;

	protected final int newStartingIndex;

	protected final int oldStartingIndex;

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a {@link NotifyCollectionChangedAction#Reset} change.
	 * 
	 * @param action
	 *            The action that caused the event. This must be set to {@link NotifyCollectionChangedAction#Reset}.
	 * @throws {@link IllegalArgumentException} If action is not Reset.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action)
	{
		super(source);
		if (NotifyCollectionChangedAction.Reset != action)
		{
			throw new IllegalArgumentException(action.toString());
		}
		this.action = action;
		this.newItems = null;
		this.oldItems = null;
		this.newStartingIndex = -1;
		this.oldStartingIndex = -1;
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a multi-item change.
	 * 
	 * @param action
	 *            The action that caused the event. This can be set to {@link NotifyCollectionChangedAction#Reset}, {@link NotifyCollectionChangedAction#Add},
	 *            or {@link NotifyCollectionChangedAction#Remove}.
	 * @param changedItems
	 *            The items that are affected by the change.
	 * @throws {@link IllegalArgumentException} If action is not Reset, Add, or Remove, or if action is Reset and changedItems is not null.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object[] changedItems)
	{
		this(source, action, changedItems, -1);
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a one-item change.
	 * 
	 * @param action
	 *            The action that caused the event. This can be set to {@link NotifyCollectionChangedAction#Reset}, {@link NotifyCollectionChangedAction#Add},
	 *            or {@link NotifyCollectionChangedAction#Remove}
	 * @param changedItem
	 *            The item that is affected by the change.
	 * @throws {@link IllegalArgumentException} If action is not Reset, Add, or Remove, or if action is Reset and changedItems is not null.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object changedItem)
	{
		this(source, action, new Object[] { changedItem });
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a multi-item {@link NotifyCollectionChangedAction#Replace}
	 * change.
	 * 
	 * @param action
	 *            The action that caused the event. This can only be set to {@link NotifyCollectionChangedAction#Replace}.
	 * @param newItems
	 *            The new items that are replacing the original items.
	 * @param oldItems
	 *            The original items that are replaced.
	 * @throws {@link IllegalArgumentException} If action is not Replace.
	 * @throws {@link IllegalArgumentException} If oldItems or newItems is null.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object[] newItems, Object[] oldItems)
	{
		this(source, action, newItems, oldItems, -1);
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a multi-item change or a
	 * {@link NotifyCollectionChangedAction#Reset} change.
	 * 
	 * @param action
	 *            The action that caused the event. This can be set to {@link NotifyCollectionChangedAction#Reset}, {@link NotifyCollectionChangedAction#Add},
	 *            or {@link NotifyCollectionChangedAction#Remove}.
	 * @param changedItems
	 *            The items affected by the change.
	 * @param startingIndex
	 *            The index where the change occurred.
	 * @throws {@link IllegalArgumentException} If action is not Reset, Add, or Remove, if action is Reset and either changedItems is not null or startingIndex
	 *         is not -1, or if action is Add or Remove and startingIndex is less than -1.
	 * @throws {@link IllegalArgumentException} If action is Add or Remove and changedItems is null.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object[] changedItems, int startingIndex)
	{
		super(source);
		this.action = action;
		if (NotifyCollectionChangedAction.Add == action)
		{
			if (changedItems == null)
			{
				throw new IllegalArgumentException("changedItems == null");
			}
			if (startingIndex < -1)
			{
				throw new IllegalArgumentException("startingIndex < -1");
			}
			newItems = changedItems;
			oldItems = null;
			newStartingIndex = startingIndex;
			oldStartingIndex = -1;
		}
		else if (NotifyCollectionChangedAction.Remove == action)
		{
			if (changedItems == null)
			{
				throw new IllegalArgumentException("changedItems == null");
			}
			if (startingIndex < -1)
			{
				throw new IllegalArgumentException("startingIndex < -1");
			}
			newItems = null;
			oldItems = changedItems;
			newStartingIndex = -1;
			oldStartingIndex = startingIndex;
		}
		else if (NotifyCollectionChangedAction.Reset == action)
		{
			if (changedItems != null)
			{
				throw new IllegalArgumentException("changedItems != null");
			}
			if (startingIndex != -1)
			{
				throw new IllegalArgumentException("startingIndex != -1");
			}
			newItems = null;
			oldItems = null;
			newStartingIndex = -1;
			oldStartingIndex = -1;
		}
		else
		{
			throw new IllegalArgumentException(action.toString());
		}
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a one-item change.
	 * 
	 * @param action
	 *            The action that caused the event. This can be set to {@link NotifyCollectionChangedAction#Reset}, {@link NotifyCollectionChangedAction#Add},
	 *            or {@link NotifyCollectionChangedAction#Remove}
	 * @param changedItem
	 *            The item that is affected by the change.
	 * @param index
	 *            The index where the change occurred.
	 * @throws {@link IllegalArgumentException} If action is not Reset, Add, or Remove, or if action is Reset and either changedItems is not null or index is
	 *         not -1.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object changedItem, int index)
	{
		this(source, action, new Object[] { changedItem }, index);
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a one-item {@link NotifyCollectionChangedAction#Replace} change.
	 * 
	 * @param action
	 *            The action that caused the event. This can only be set to {@link NotifyCollectionChangedAction#Replace}.
	 * @param newItem
	 *            The new item that is replacing the original item.
	 * @param oldItem
	 *            The original item that is replaced.
	 * @throws {@link IllegalArgumentException} If action is not Replace.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object newItem, Object oldItem)
	{
		this(source, action, new Object[] { newItem }, new Object[] { oldItem }, -1);
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a multi-item {@link NotifyCollectionChangedAction#Replace}
	 * change.
	 * 
	 * @param action
	 *            The action that caused the event. This can only be set to {@link NotifyCollectionChangedAction#Replace}.
	 * @param newItems
	 *            The new items that are replacing the original items.
	 * @param oldItems
	 *            The original items that are replaced.
	 * @param startingIndex
	 *            The index of the first item of the items that are being replaced.
	 * @throws {@link IllegalArgumentException} If action is not Replace.
	 * @throws {@link IllegalArgumentException} If oldItems or newItems is null.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object[] newItems, Object[] oldItems, int startingIndex)
	{
		super(source);
		if (NotifyCollectionChangedAction.Replace != action)
		{
			throw new IllegalArgumentException(action.toString());
		}
		if (newItems == null)
		{
			throw new IllegalArgumentException("newItems == null");
		}
		if (oldItems == null)
		{
			throw new IllegalArgumentException("oldItems == null");
		}
		this.action = action;
		this.newItems = newItems;
		this.oldItems = oldItems;
		this.newStartingIndex = startingIndex;
		this.oldStartingIndex = startingIndex;
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a multi-item {@link NotifyCollectionChangedAction#Move} change.
	 * 
	 * @param action
	 *            The action that caused the event. This can only be set to {@link NotifyCollectionChangedAction#Move} .
	 * @param changedItems
	 *            The items affected by the change.
	 * @param newIndex
	 *            The new index for the changed items.
	 * @param oldIndex
	 *            The old index for the changed items.
	 * @throws {@link IllegalArgumentException} If action is not Move or index is less than 0.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object[] changedItems, int newIndex, int oldIndex)
	{
		super(source);
		if (NotifyCollectionChangedAction.Move != action)
		{
			throw new IllegalArgumentException(action.toString());
		}
		if (newIndex < 0)
		{
			throw new IllegalArgumentException("newIndex < 0");
		}
		if (oldIndex < 0)
		{
			throw new IllegalArgumentException("oldIndex < 0");
		}
		this.action = action;
		this.newItems = changedItems;
		this.oldItems = changedItems;
		this.newStartingIndex = newIndex;
		this.oldStartingIndex = oldIndex;
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a multi-item {@link NotifyCollectionChangedAction#Move} change.
	 * 
	 * @param action
	 *            The action that caused the event. This can only be set to {@link NotifyCollectionChangedAction#Move}
	 * @param changedItem
	 *            The item affected by the change.
	 * @param newIndex
	 *            The new index for the changed item.
	 * @param oldIndex
	 *            The old index for the changed item.
	 * @throws {@link IllegalArgumentException} If action is not Move or index is less than 0.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object changedItem, int newIndex, int oldIndex)
	{
		this(source, action, new Object[] { changedItem }, newIndex, oldIndex);
	}

	/**
	 * Initializes a new instance of the NotifyCollectionChangedEventArgs class that describes a one-item {@link NotifyCollectionChangedAction#Replace} change.
	 * 
	 * @param action
	 *            The action that caused the event. This can be set to {@link NotifyCollectionChangedAction#Replace}.
	 * @param newItem
	 *            The new item that is replacing the original item.
	 * @param oldItem
	 *            The original item that is replaced.
	 * @param index
	 *            The index of the item being replaced.
	 * @throws {@link IllegalArgumentException} If action is not Replace.
	 */
	public NotifyCollectionChangedEvent(Object source, NotifyCollectionChangedAction action, Object newItem, Object oldItem, int index)
	{
		this(source, action, new Object[] { newItem }, new Object[] { oldItem }, index);
	}

	/**
	 * Gets the action that caused the event.
	 * 
	 * @return A {@link NotifyCollectionChangedAction} value that describes the action that caused the event.
	 */
	public NotifyCollectionChangedAction getAction()
	{
		return action;
	}

	/**
	 * Gets the list of new items involved in the change.
	 * 
	 * @return The list of new items involved in the change.
	 */
	public Object[] getNewItems()
	{
		return newItems;
	}

	/**
	 * Gets the list of items affected by a {@link NotifyCollectionChangedAction} Replace, Remove, or Move action.
	 * 
	 * @return The list of items affected by a {@link NotifyCollectionChangedAction} Replace, Remove, or Move action.
	 */
	public Object[] getOldItems()
	{
		return oldItems;
	}

	/**
	 * Gets the index at which the change occurred.
	 * 
	 * @return The zero-based index at which the change occurred.
	 */
	public int getNewStartingIndex()
	{
		return newStartingIndex;
	}

	/**
	 * Gets the index at which a {@link NotifyCollectionChangedAction} Move, Remove, or Replace action occurred.
	 * 
	 * @return The zero-based index at which a {@link NotifyCollectionChangedAction} Move, Remove, or Replace action occurred.
	 */
	public int getOldStartingIndex()
	{
		return oldStartingIndex;
	}
}
