using System;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Collections.Specialized
{
    /**
     * Represents the method that handles the {@link INotifyCollectionChanged} collectionChanged event
     */
    public interface IPropertyChangedEventHandler
    {
        /**
         * This method gets called when an ObservableCollection is changed.
         * 
         * @param evt
         *            A {@link NotifyCollectionChangedEvent} object describing the event source and the collection that has changed.
         */
        void PropertyChanged(Object sender, PropertyChangedEventArgs evt);
    }
}
