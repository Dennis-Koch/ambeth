using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Threading;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using AmbethIDataObject = De.Osthus.Ambeth.Model.IDataObject;

namespace De.Osthus.Minerva.Core
{
    public abstract class GenericViewModel : IGenericViewModel, IInitializingBean
    {
        public const string CONST_SELECTED_OBJECT = "SelectedObject";
        public const string CONST_OBJECTS = "Objects";
        public const string CONST_IS_BUSY = "IsBusy";

        public event PropertyChangedEventHandler PropertyChanged;

        // Fired whenever the number of changed objects changes:
        public event PropertyChangedEventHandler NotPersistedChanged;

        public IEntityMetaDataProvider EntityMetaDataProvider { get; set; }

        protected int maxPageSize = int.MaxValue;
        public virtual int MaxPageSize
        {
            get
            {
                return maxPageSize;
            }
            set
            {
                maxPageSize = value;
            }
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
        }

        protected virtual void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        protected bool isBusy;
        public bool IsBusy
        {
            get { return isBusy; }
            set
            {
                if (isBusy != value)
                {
                    isBusy = value;
                    OnPropertyChanged(CONST_IS_BUSY);
                }
            }
        }
        
        protected int pageSize = 1;
        protected int pageIndex;
        protected int itemCount;

        public virtual int PageSize
        {
            get
            {
                return pageSize;
            }
            set
            {
                if (value == pageSize)
                {
                    return;
                }
                if (value < 1)
                {

                    PageSize = 1;
                    return;
                }
                else if (value > MaxPageSize)
                {
                    PageSize = MaxPageSize;
                    return;
                }
                else
                {
                    pageSize = value;
                }
                OnPropertyChanged("PageSize");
            }
        }

        public virtual int PageIndex
        {
            get
            {
                return pageIndex;
            }
            set
            {
                if (value == pageIndex)
                {
                    return;
                }
                if (value < 0)
                {
                    PageIndex = 0;
                    return;
                }
                else
                {
                    pageIndex = value;
                }
                OnPropertyChanged("PageIndex");
            }
        }

        public virtual int ItemCount
        {
            get
            {
                return itemCount;
            }
            set
            {
                if (value == itemCount)
                {
                    return;
                }
                itemCount = value;
                OnPropertyChanged("ItemCount");
            }
        }

        public abstract IList<Object> GetNotPersistedDataRaw();

        public abstract bool HasNotPersisted();

        public abstract int Count { get; }

        public abstract IEnumerable ValuesData { get; }

        //public abstract Object ValueData { get; set; }

        public PropertyChangedEventHandler GetNotPersistedChanged()
        {
            return NotPersistedChanged;
        }
    }


    public class GenericViewModel<T> : GenericViewModel, IGenericViewModel<T>
    {
        protected static readonly PropertyChangedEventArgs notPersistedEventArgs = new PropertyChangedEventArgs("NotPersisted");
        protected static readonly PropertyChangedEventArgs justPersistedEventArgs = new PropertyChangedEventArgs("JustPersisted");

        public virtual event NotifyCollectionChangedEventHandler CollectionChanged;

        // Unchanged objects of the current view will be tracked for changes:
        protected readonly IdentityHashSet<INotifyPropertyChanged> trackedUnchangedObjects = new IdentityHashSet<INotifyPropertyChanged>();

        // All changed objects will be tracked for further changes:
        protected readonly IdentityHashSet<INotifyPropertyChanged> trackedChangedObjects = new IdentityHashSet<INotifyPropertyChanged>();

        // It is necessary to keep ToBeCreated items in an additional set in order to test on property changed whether these items were persisted: 
        protected readonly IdentityHashSet<INotifyPropertyChanged> toBeCreated = new IdentityHashSet<INotifyPropertyChanged>();

        protected readonly PropertyChangedEventHandler onItemPropertyChangedDelegate;

        protected readonly ObservableCollection<T> objects = new ObservableCollection<T>();

        protected readonly IList<T> objectsReadOnly;

        public virtual IGuiThreadHelper GuiThreadHelper { get; set; }

        public GenericViewModel()
        {
            onItemPropertyChangedDelegate = new PropertyChangedEventHandler(OnItemPropertyChanged);
            objectsReadOnly = new ReadOnlyObservableCollection<T>(objects);
        }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
        	// Observe changes in objects to check whether not persisted data is added:
            objects.CollectionChanged += OnObjectsChanged;
        }

        public virtual IList<T> Objects
        {
            get
            {
                return objects;
            }
            set
            {
                if (objects.Count == 0 && (value == null || value.Count == 0))
                {
                    return;
                }
                int oldCount = objects.Count;
                if (oldCount == value.Count)
                {
                    // Potentially nothing changed
                    bool sameList = true;
                    for (int a = oldCount; a-- > 0; )
                    {
                        if (!Object.ReferenceEquals(objects[a], value[a]))
                        {
                            sameList = false;
                            break;
                        }
                    }
                    if (sameList)
                    {
                        // Nothing to do. New collection set is identical to existing
                        return;
                    }
                }
                objects.Clear();
                if (value != null)
                {
                    for (int a = 0, size = value.Count; a < size; a++)
                    {
                        T newObject = value[a];
                        objects.Add(newObject);
                    }
                }
                if (oldCount != objects.Count)
                {
                    OnPropertyChanged("Count");
                }
                OnPropertyChanged("Objects");
                OnPropertyChanged("Values");
                OnPropertyChanged("ValuesData");
            }
        }

        public virtual IList<T> GetClonedObjects()
        {
            return GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                return new List<T>(objects);
            });
        }

        public virtual void Add(T newObject)
        {
            int index = objects.Count;
            objects.Add(newObject);
            if (CollectionChanged != null)
            {
                CollectionChanged.Invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Add, newObject, index));
            }
        }

        public virtual void RemoveAt(int index)
        {
            T oldObject = objects[index];
            objects.RemoveAt(index);
            if (CollectionChanged != null)
            {
                CollectionChanged.Invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Remove, oldObject, index));
            }
        }

        public virtual void InsertAt(int index, T newObject)
        {
            objects.Insert(index, newObject);
            if (CollectionChanged != null)
            {
                CollectionChanged.Invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Add, newObject, index));
            }
        }

        public virtual void Replace(int index, T newObject)
        {
            T oldObject = objects[index];
            objects[index] = newObject;
            if (CollectionChanged != null)
            {
                CollectionChanged.Invoke(this, new NotifyCollectionChangedEventArgs(NotifyCollectionChangedAction.Replace, newObject, oldObject, index));
            }
        }

        public virtual void Clear()
        {
            if (objects.Count > 0)
            {
                objects.Clear();
                OnPropertyChanged("Objects");
            }
        }

        // Remove an item if it is tracked and also deregister from property changed event:
        protected virtual bool RemoveIfTracked(Object item)
        {
            if (!(item is INotifyPropertyChanged))
            {
                return false;
            }
            INotifyPropertyChanged npc = (INotifyPropertyChanged)item;
            if (trackedChangedObjects.Remove(npc))
            {
                npc.PropertyChanged -= onItemPropertyChangedDelegate;
                if (toBeCreated.Remove(npc))
                {
                    PropertyChangedEventHandler eventHandler = GetNotPersistedChanged();
                    if (eventHandler != null)
                    {
                        eventHandler.Invoke(this, justPersistedEventArgs);
                    }
                }
                return true;
            }
            else if (trackedUnchangedObjects.Remove(npc))
            {
                npc.PropertyChanged -= onItemPropertyChangedDelegate;
                return true;
            }
            return false;
        }

        // Add an item to the collection of tracked items and register on its property changed event:
        protected virtual bool AddIfUntracked(Object item)
        {
            if (!(item is INotifyPropertyChanged) || !(item is AmbethIDataObject))
            {
                return false;
            }
            INotifyPropertyChanged npc = (INotifyPropertyChanged)item;
            if (trackedChangedObjects.Contains(npc) || trackedUnchangedObjects.Contains(npc))
            {
                return false;
            }
            npc.PropertyChanged += onItemPropertyChangedDelegate;
            AmbethIDataObject iDataObj = (AmbethIDataObject)item;
            if (iDataObj.HasPendingChanges)
            {
                trackedChangedObjects.Add(npc);
                if (iDataObj.ToBeCreated)
                {
                    toBeCreated.Add(npc);
                }
            }
            else
            {
                trackedUnchangedObjects.Add(npc);
            }
            return true;
        }

        // A created item that was just persisted must be removed from the view model
        // and must no longer be tracked:
        protected virtual void RemoveNewlyPersistedFromVM(T item)
        {
            // RemoveIfTracked is redundant because each RemoveAt(index) will call this method anyway
            // RemoveIfTracked(item);
            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                int index = Objects.IndexOf(item);
                if (index != -1)
                {
                    RemoveAt(index);
                }
                else
                {
                    // The VMDCC
                    //  a) cleared the objects collection
                    //  b) re-added objects, but not the changed item
                    //  c) changed the item (that's why we are here)
                    //  => Now we must remove the item from the tracking, because in the next step ambeth will add the modified (persisted) version.
                    //     If then another change to the Item is applied in would be removed in this method.
                    RemoveIfTracked(item);
                }
                PropertyChangedEventHandler eventHandler = GetNotPersistedChanged();
                if (eventHandler != null)
                {
                    eventHandler.Invoke(this, justPersistedEventArgs);
                }
            });
        }

        // Event handler for property changed on tracked items:
        public virtual void OnItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
        {
            // Tracked objects are ALWAYS IDataObjects:
            AmbethIDataObject item = (AmbethIDataObject)sender;
            INotifyPropertyChanged npc = (INotifyPropertyChanged)item;
            if (toBeCreated.Contains(npc))
            {
                if (!item.ToBeCreated)
                {
                    // This formerly ToBeCreated item got a primary key (was persisted)
                    // => deregister from property changed event and remove from view model (the VMDCC will re-insert it subsequently):
                    RemoveNewlyPersistedFromVM((T)item);
                }
                return;
            }
            if (e.PropertyName != "ToBeDeleted" &&
                e.PropertyName != "ToBeUpdated")
            {
                return;
            }
            PropertyChangedEventArgs usedEventArgs = null;
            if (trackedUnchangedObjects.Contains(npc))
            {
                // A formerly unchanged item was changed => remove from trackedUnchanged and add to trackedChanged:
                trackedUnchangedObjects.Remove(npc);
                trackedChangedObjects.Add(npc);
                usedEventArgs = notPersistedEventArgs;
            }
            else if (!item.HasPendingChanges)
            {
                // Changes to an item were persisted => remove from trackedChanged:
                RemoveIfTracked(item);
                usedEventArgs = justPersistedEventArgs;
                if (Objects.Contains((T)item))
                {
                    // item remains in the view => add to trackedUnchanged:
                    AddIfUntracked(item);
                }
            }
            if (usedEventArgs == null)
            {
                // An object with local changes was changed again:
                return;
            }
            PropertyChangedEventHandler eventHandler = GetNotPersistedChanged();
            if (eventHandler != null)
            {
                GuiThreadHelper.InvokeInGuiAndWait(delegate()
                {
                    eventHandler.Invoke(this, usedEventArgs);
                });
            }
        }

        // Handle changes in the objects collection:
        public virtual void OnObjectsChanged(Object sender, NotifyCollectionChangedEventArgs e)
        {
            if (e.Action == NotifyCollectionChangedAction.Reset)
            {
                // A reset is a significant change in the collection (mostly a clear)
                // => Remove all tracked but unchanged items and then readd possibly
                //    remaining unchanged from the objects collection:
                IList<Object> toRemove = new List<Object>(trackedUnchangedObjects.Count);
                foreach (Object item in trackedUnchangedObjects)
                {
                    toRemove.Add(item);
                }
                foreach (Object item in toRemove)
                {
                    RemoveIfTracked(item);
                }
                foreach (Object item in Objects)
                {
                    AddIfUntracked(item);
                }
            }
            else
            {
                if (e.Action == NotifyCollectionChangedAction.Add || e.Action == NotifyCollectionChangedAction.Replace)
                {
                    bool fireToBeCreated = false;
                    foreach (T item in e.NewItems)
                    {
                        // New items will always be tracked via their PropertyChanged event:
                        if (!AddIfUntracked(item))
                        {
                            // item is either no IDataObject or it is already tracked or it
                            // does not implement the property changed event:
                            continue;
                        }
                        if (((AmbethIDataObject)item).ToBeCreated)
                        {
                            fireToBeCreated = true;
                        }
                    }
                    if (fireToBeCreated)
                    {
                        PropertyChangedEventHandler eventHandler = GetNotPersistedChanged();
                        if (eventHandler != null)
                        {
                            eventHandler.Invoke(this, notPersistedEventArgs);
                        }
                    }
                }
                if (e.Action == NotifyCollectionChangedAction.Remove || e.Action == NotifyCollectionChangedAction.Replace)
                {
                    foreach (T item in e.OldItems)
                    {
                        // Unchanged items that are removed from the view must no longer be tracked.
                        // If a single item with pending changes is removed from the objects collection,
                        // it must be a ToBeCreated (e.g. removed due to a RevertChanges), so it also
                        // must no longer be tracked.
                        // In case of paging, changed items must be tracked, even if they are removed from
                        // the current view. However, the e.Action on paging should always be a Reset.
                        RemoveIfTracked(item);
                    }
                }
            }
        }
        
        public virtual void ClearChangedObjects()
        {
            IList<Object> toBeRemoved = new List<Object>(trackedChangedObjects.Count);
            foreach (Object item in trackedChangedObjects)
            {
                toBeRemoved.Add(item);
            }
            foreach (Object item in toBeRemoved)
            {
                RemoveIfTracked(item);
            }
        }

        // This function returns a list with all not persisted items:
        public virtual IList<T> GetNotPersistedData()
        {
            IList<T> result = new List<T>(trackedChangedObjects.Count);
            foreach (T item in trackedChangedObjects)
            {
                result.Add(item);
            }
            return result;
        }

        public override IList<Object> GetNotPersistedDataRaw()
        {
            IList<Object> result = new List<Object>(trackedChangedObjects.Count);
            foreach (Object item in trackedChangedObjects)
            {
                result.Add(item);
            }
            return result;
        }

        // If not a list of not persisted items is needed, but only the information whether
        // there exists such data, the following method should be used:
        public override bool HasNotPersisted()
        {
            return (trackedChangedObjects.Count > 0);
        }

        public override int Count
        {
            get
            {
                return objects.Count;
            }
        }

        public virtual IList<T> Values
        {
            get
            {
                return objectsReadOnly;
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public override IEnumerable ValuesData
        {
            get
            {
                return Objects;
            }
        }
    }
}
