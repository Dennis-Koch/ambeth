using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Collections.Specialized
{
    public class UsableObservableCollection<T> : ObservableCollection<T>
    {
        public UsableObservableCollection() : base()
        {
            // Intended blank
        }
        public UsableObservableCollection(IEnumerable<T> collection) : base(collection)
        {
            // Intended blank
        }

        public UsableObservableCollection(List<T> list) : base(list)
        {
            // Intended blank
        }

        /// <summary>
        /// Clears all items in the collection by removing them individually.
        /// </summary>
        protected override void ClearItems()
        {
            IList<T> items = new List<T>(this);
            foreach (T item in items)
            {
                Remove(item);
            }
        }

        //// overriden so that we can call GetInvocationList
        //public override event NotifyCollectionChangedEventHandler CollectionChanged;

        //protected override void OnCollectionChanged(NotifyCollectionChangedEventArgs e)
        //{
            //NotifyCollectionChangedEventHandler collectionChanged = CollectionChanged;
            //if (collectionChanged == null)
            //{
            //    return;
            //}
            //NotifyCollectionChangedEventArgs resetEvnt = null;
            //foreach (NotifyCollectionChangedEventHandler handler in collectionChanged.GetInvocationList())
            //{
            //    try
            //    {
            //        handler(this, e);
            //    }
            //    catch (NotSupportedException ex)
            //    {
            //        // this will occur if this collection is used as an ItemsControl.ItemsSource
            //        if (ex.Message == "Range actions are not supported.")
            //        {
            //            if (resetEvnt == null)
            //            {
            //                resetEvnt = new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Reset);
            //            }
            //            handler(this, resetEvnt);
            //        }
            //        else
            //        {
            //            throw ex;
            //        }
            //    }
            //}
        //}
    }
}
