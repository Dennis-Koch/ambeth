using System.Collections.ObjectModel;

namespace De.Osthus.Ambeth.Proxy
{
    public class DefaultObservableList<T> : ObservableCollection<T>, IDefaultCollection
    {
        protected bool hasDefaultState = true;
        
	    public bool HasDefaultState
	    {
            get
            {
                return hasDefaultState;
            }
	    }

        protected override void InsertItem(int index, T item)
        {
            hasDefaultState = false;
            base.InsertItem(index, item);
        }

        protected override void SetItem(int index, T item)
        {
            hasDefaultState = false;
            base.SetItem(index, item);
        }

        protected override void RemoveItem(int index)
        {
            hasDefaultState = false;
            base.RemoveItem(index);
        }
    }
}