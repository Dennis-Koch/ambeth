using System;
using System.Collections.Specialized;

namespace De.Osthus.Ambeth.Collections.Specialized
{
    /**
     * Represents the method that handles the {@link INotifyCollectionChanged} collectionChanged event
     */
    public interface INotifyCollectionChangedListener
    {
	    /**
	     * This method gets called when an ObservableCollection is changed.
	     * 
	     * @param evt
	     *            A {@link NotifyCollectionChangedEvent} object describing the event source and the collection that has changed.
	     */
	    void CollectionChanged(Object sender, NotifyCollectionChangedEventArgs evt);
    }
}
