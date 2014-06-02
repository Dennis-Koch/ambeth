using De.Osthus.Ambeth.Event;
using System;
using System.Collections.Generic;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Collections.Specialized
{
    /**
     * This is a utility class that manages a list of listeners and dispatches {@link NotifyCollectionChangedEvent}s to them. You can use an instance of this class
     * as a member field of your collection and delegate these types of work to it. The {@link INotifyCollectionChangedListener} can be registered for the
     * collection
     */
    public class PropertyChangeSupport : List<PropertyChangedEventHandler>, INotifyPropertyChanged
    {
	    private static readonly PropertyChangedEventHandler[] EMPTY_LISTENERS = new PropertyChangedEventHandler[0];

	    private PropertyChangedEventHandler[] listenersCopy = EMPTY_LISTENERS;

	    public PropertyChangeSupport() : base(1)
	    {
		    // Intended blank
	    }

        public event PropertyChangedEventHandler PropertyChanged
        {
            add
            {
                AddPropertyChangeListener(value);
            }
            remove
            {
                RemovePropertyChangeListener(value);
            }
        }

	    /**
	     * Add a NotifyCollectionChangedListener to the listener list. The listener is registered for all properties. The same listener object may be added more
	     * than once, and will be called as many times as it is added. If <code>listener</code> is null, no exception is thrown and no action is taken.
	     * 
	     * @param listener
	     *            The NotifyCollectionChangedListener to be added
	     */
	    public void AddPropertyChangeListener(PropertyChangedEventHandler listener)
	    {
		    if (listener == null)
		    {
			    return;
		    }
            Add(listener);
		    listenersCopy = ToArray();
	    }

	    /**
	     * Remove a NotifyCollectionChangedListener from the listener list. This removes a NotifyCollectionChangedListener that was registered for all properties.
	     * If <code>listener</code> was added more than once to the same event source, it will be notified one less time after being removed. If
	     * <code>listener</code> is null, or was never added, no exception is thrown and no action is taken.
	     * 
	     * @param listener
	     *            The NotifyCollectionChangedListener to be removed
	     */
	    public void RemovePropertyChangeListener(PropertyChangedEventHandler listener)
	    {
		    if (listener == null)
		    {
			    return;
		    }
		    if (!Remove(listener))
		    {
			    return;
		    }
		    if (Count == 0)
		    {
			    listenersCopy = EMPTY_LISTENERS;
		    }
		    else
		    {
			    listenersCopy = ToArray();
		    }
	    }

	    /**
	     * Returns an array of all the listeners that were added to the NotifyCollectionChangedSupport object with addNotifyCollectionChangedListener().
	     * 
	     * @return all of the <code>NotifyCollectionChangedListeners</code> added or an empty array if no listeners have been added
	     */
	    public PropertyChangedEventHandler[] GetPropertyChangeListeners()
	    {
		    if (Count == 0)
		    {
			    return EMPTY_LISTENERS;
		    }
		    return ToArray();
	    }

	    /**
	     * Fires a property change event to listeners that have been registered to track updates of collection.
	     * 
	     * @param event
	     *            the {@code NotifyCollectionChangedEvent} to be fired
	     */
        public void FirePropertyChange(Object obj, PropertyChangedEventArgs evntArg, String propertyName, Object oldValue, Object currentValue)
        {
            PropertyChangedEventHandler[] listenersCopy = this.listenersCopy;
		    if (listenersCopy.Length == 0)
		    {
			    return;
		    }
		    //PropertyChangeEvent evnt = new PropertyChangeEvent(obj, propertyName, oldValue, currentValue);
            foreach (PropertyChangedEventHandler listener in listenersCopy)
		    {
                listener(obj, evntArg);
		    }
        }
    }
}