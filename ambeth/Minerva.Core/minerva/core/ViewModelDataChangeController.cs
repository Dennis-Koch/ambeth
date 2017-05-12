using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.FilterProvider;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Minerva.Core
{
    public class ViewModelDataChangeController<T> : IViewModelDataChangeController<T>, IViewModelDataChangeController, IDataChangeListener, IInitializingBean, IStartingBean, IDisposableBean where T : class
    {
        public static readonly Object[] EMPTY_CONTEXT = new Object[0];

        [LogInstance]
        public ILogger Log { private get; set; }

        protected Object currentRequest;
        protected Object currentRequestLock = new Object();

        public SynchronizationContext SyncContext { protected get; set; }

        public IDictionary<Type, IList<String>> InitializedRelations { protected get; set; }

        public IConversionHelper ConversionHelper { protected get; set; }

        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        public IProxyHelper ProxyHelper { protected get; set; }

        public GenericViewModel<T> Model { protected get; set; }

        public IBaseRefresher<T> Refresher { protected get; set; }

        public IThreadPool ThreadPool { protected get; set; }

        public bool AutoPopulate { protected get; set; }

        public bool ToBeCreatedOnTop { protected get; set; }

        public ICache Cache { protected get; set; }

        public ICacheProvider CacheProvider { protected get; set; }

        public ICacheContext CacheContext { protected get; set; }

		// This provider is used to fill the context information with an ambeth filter descriptor
        public IFilterDescriptorProvider FilterDescriptorProvider { protected get; set; }
		
		// This provider is used to fill the context information with an ambeth sort descriptor
        public ISortDescriptorProvider SortDescriptorProvider { protected get; set; }

        public Type[] InterestedEntityTypes { protected get; set; }
        
        // For fast check whether a paged view model is used and whether Filter and/or Sort information is available:
        protected bool hasPagedViewModel;

        protected static readonly ReadWriteLock rwLock = new ReadWriteLock();

        public ViewModelDataChangeController()
        {
            AutoPopulate = true;
        }

        #region Lifecycle
        
        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Cache, "Cache");
            ParamChecker.AssertNotNull(CacheContext, "CacheContext");
            ParamChecker.AssertNotNull(ConversionHelper, "ConversionHelper");
            ParamChecker.AssertNotNull(CacheProvider, "CacheProvider");
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
            ParamChecker.AssertNotNull(ProxyHelper, "ProxyHelper");
            ParamChecker.AssertNotNull(Model, "Model");
            ParamChecker.AssertNotNull(Refresher, "Refresher");
            ParamChecker.AssertNotNull(ThreadPool, "ThreadPool");

            if (InterestedEntityTypes == null)
            {
                InterestedEntityTypes = new Type[] { typeof(T) };
            }
            else if (!new List<Type>(InterestedEntityTypes).Contains(typeof(T)))
            {
                List<Type> typesList = new List<Type>(InterestedEntityTypes);
                typesList.Add(typeof(T));
                InterestedEntityTypes = typesList.ToArray();
            }

            hasPagedViewModel = false;
            if (Refresher is IPagingRefresher<T>)
            {
                hasPagedViewModel = true;
            }
            if (Refresher is INotifyDataAvailable)
            {
                ((INotifyDataAvailable)Refresher).DataAvailable += OnNewDataFromRefresherAvailable;
            }
        }

        public virtual void AfterStarted()
        {
            if (AutoPopulate)
            {
                Populate();
            }
        }

        public virtual void Destroy()
        {
            if (Refresher is INotifyDataAvailable)
            {
                ((INotifyDataAvailable)Refresher).DataAvailable -= OnNewDataFromRefresherAvailable;
            }
        }

        #endregion

        // If the refresher implements INotifyDataAvailable, the corresponding event is handled here:
        protected virtual void OnNewDataFromRefresherAvailable(Object sender, EventArgs e)
        {
            Populate();
        }

        public virtual void AddRelationPath(String relationPath)
        {
            AddRelationPath<T>(relationPath);
        }

        public virtual void AddRelationPath<R>(String relationPath) where R : class
        {
            if (InitializedRelations == null)
            {
                InitializedRelations = new Dictionary<Type, IList<String>>();
            }
            IList<String> pathNames = DictionaryExtension.ValueOrDefault(InitializedRelations, typeof(R));
            if (pathNames == null)
            {
                pathNames = new List<String>();
                InitializedRelations.Add(typeof(R), pathNames);
            }
            if (!pathNames.Contains(relationPath))
            {
                pathNames.Add(relationPath);
            }
        }

        // After each Find, the paging properties of the viewmodel must be updated:
        public virtual void UpdatePagingInformation(IPagingResponse pagingResponse)
        {
            if (pagingResponse != null)
            {
                Model.ItemCount = pagingResponse.TotalSize;
                Model.PageIndex = pagingResponse.Number;
            }
        }

        // Refresh on changes in paging properties from viewmodel:
        // (example: beanContextFactory.LinkToEvent<INotifyPropertyChanged>("model_MyEntity", "vMDCC_MyEntity", "OnViewModelPaging"); )
        public virtual void OnViewModelPaging(Object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName == "PageSize" || e.PropertyName == "PageIndex")
            {
                Populate();
            }
        }

        // Set first page and refresh on changes in filter or sort descriptors:
        // (Should be necessary only with paging!)
        // (example: beanContextFactory.LinkToEvent<INotifyPropertyChanged>("gridViewFilterDescriptorProvider", "vMDCC_MyEntity", "OnDescriptorChanged"); )
        public virtual void OnDescriptorChanged(Object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName == "AmbethFilterDescriptor" || e.PropertyName == "SortDescriptorList")
            {
                // If sorting or filtering has changed, the first page shall be displayed:
                if (Model.PageIndex == 0)
                {
                    Populate();
                }
                else
                {
                    Model.PageIndex = 0;
                }
            }
        }

        // Important to update the sorting states (if the provider disables gridviews own sorting mechanism):
        public virtual void UpdateAfterDCE()
        {
            if (SortDescriptorProvider != null)
            {
                SortDescriptorProvider.DisplayCurrentSortStates();
            }
        }

        public virtual void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            dataChange = dataChange.Derive(InterestedEntityTypes);
            if (dataChange.IsEmpty)
            {
                return;
            }
            ISet<Object> directObjectsToDelete = null;

            ISet<Type> requestedTypes = new HashSet<Type>();
            IDictionary<Type, IEntityMetaData> typeToMetaDataDict = new Dictionary<Type, IEntityMetaData>();

            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                IList<T> entities = Model.Objects;

                for (int i = entities.Count; i-- > 0; )
                {
                    Object entity = entities[i];

                    requestedTypes.Add(entity.GetType());
                }
            });

            IList<IDataChangeEntry> dataChangeEntries = dataChange.Inserts;
            for (int a = dataChangeEntries.Count; a-- > 0; )
            {
                requestedTypes.Add(dataChangeEntries[a].EntityType);
            }
            dataChangeEntries = dataChange.Updates;
            for (int a = dataChangeEntries.Count; a-- > 0; )
            {
                requestedTypes.Add(dataChangeEntries[a].EntityType);
            }
            dataChangeEntries = dataChange.Deletes;
            for (int a = dataChangeEntries.Count; a-- > 0; )
            {
                requestedTypes.Add(dataChangeEntries[a].EntityType);
            }

            IList<IEntityMetaData> metaDatas = EntityMetaDataProvider.GetMetaData(ListUtil.ToList(requestedTypes));
            foreach (IEntityMetaData metaData in metaDatas)
            {
                typeToMetaDataDict[metaData.EntityType] = metaData;
            }

            bool consistsOnlyOfDirectDeletes = false;
            if (dataChange.Deletes.Count > 0)
            {
                consistsOnlyOfDirectDeletes = true;
                foreach (IDataChangeEntry deleteEntry in dataChange.Deletes)
                {
                    if (deleteEntry is DirectDataChangeEntry)
                    {
                        if (directObjectsToDelete == null)
                        {
                            directObjectsToDelete = new IdentityHashSet<Object>();
                        }
                        directObjectsToDelete.Add(((DirectDataChangeEntry)deleteEntry).Entry);
                    }
                    else
                    {
                        consistsOnlyOfDirectDeletes = false;
                    }
                }
            }

            IList<T> interestingEntities = null;

            Object[] contextInformation = GetContextInformation();
            IFilterDescriptor filterDescriptor = GetFilterDescriptor();
            IList<ISortDescriptor> sortDescriptors = GetSortDescriptors();
            IPagingRequest pagingRequest = GetPagingRequest();

            IPagingResponse pagingResponse = null;
            List<IDataChangeEntry> modifiedEntries = new List<IDataChangeEntry>();
            modifiedEntries.AddRange(dataChange.All);

            if (!consistsOnlyOfDirectDeletes)
            {
                interestingEntities = CacheContext.ExecuteWithCache(CacheProvider.GetCurrentCache(), delegate()
                {
                    ConfigureCacheWithEagerLoads(Cache);
                    if (Refresher is IPagingRefresher<T>)
                    {
                        interestingEntities = new List<T>();
                        pagingResponse = ((IPagingRefresher<T>)Refresher).Refresh(modifiedEntries, filterDescriptor, sortDescriptors, pagingRequest, contextInformation);
                        foreach (Object obj in pagingResponse.Result)
                        {
                            interestingEntities.Add((T)obj);
                        }
                        return interestingEntities;
                    }
                    else
                    {
                        if (filterDescriptor != null || sortDescriptors != null)
                        {
                            contextInformation = new Object[2];
                            contextInformation[0] = filterDescriptor;
                            contextInformation[1] = sortDescriptors;
                        }

                        return ((IRefresher<T>)Refresher).Refresh(modifiedEntries, contextInformation);
                    }
                });
            }
            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                IList<T> entities = Model.Objects;

                ISet<T> entitiesToAdd = null;
                ISet<T> entitiesToRemove = null;
                IDictionary<T, T> entitiesToReplace = null;
                IDictionary<IObjRef, T> oldObjRefToOldEntityMap = null;
                bool mergeModel = false;

                if (interestingEntities != null && interestingEntities.Count > 0)
                {
                    entitiesToAdd = new IdentityHashSet<T>(interestingEntities);
                    entitiesToRemove = new IdentityHashSet<T>(entities);
                    entitiesToReplace = new IdentityDictionary<T, T>();
                    oldObjRefToOldEntityMap = new Dictionary<IObjRef, T>();
                    mergeModel = true;
                }
                for (int i = entities.Count; i-- > 0; )
                {
                    T oldEntity = entities[i];
                    if (directObjectsToDelete != null && directObjectsToDelete.Contains(oldEntity))
                    {
                        if (entitiesToRemove != null)
                        {
                            entitiesToRemove.Remove(oldEntity);
                        }
                        Model.RemoveAt(i);
                        continue;
                    }
                    Type oldEntityType = ProxyHelper.GetRealType(oldEntity.GetType());
                    PrimitiveMember idMember = typeToMetaDataDict[oldEntityType].IdMember;
                    Object oldEntityId = idMember.GetValue(oldEntity, false);
                    if (oldEntityId == null)
                    {
                        if (entitiesToRemove != null)
                        {
                            entitiesToRemove.Remove(oldEntity);
                        }
                        // Unpersisted object. This object should not be removed
                        // only because of a background DCE
                        continue;
                    }
                    bool entryRemoved = false;
                    foreach (IDataChangeEntry deleteEntry in dataChange.Deletes)
                    {
                        if (deleteEntry is DirectDataChangeEntry)
                        {
                            continue;
                        }
                        Object id = deleteEntry.Id;
                        if (!EqualsItems(oldEntityType, oldEntityId, deleteEntry.EntityType, id))
                        {
                            continue;
                        }
                        if (entitiesToRemove != null)
                        {
                            entitiesToRemove.Remove(oldEntity);
                        }
                        Model.RemoveAt(i);
                        entryRemoved = true;
                        break;
                    }
                    if (entryRemoved)
                    {
                        continue;
                    }
                    if (mergeModel)
                    {
                        IObjRef oldObjRef = new ObjRef(oldEntityType, ObjRef.PRIMARY_KEY_INDEX, oldEntityId, null);
                        T existingOldEntity = DictionaryExtension.ValueOrDefault(oldObjRefToOldEntityMap, oldObjRef);
                        if (existingOldEntity == null)
                        {
                            oldObjRefToOldEntityMap.Add(oldObjRef, oldEntity);
                        }
                        else if (!Object.ReferenceEquals(existingOldEntity, oldEntity))
                        {
                            // Force duplicate key exception
                            oldObjRefToOldEntityMap.Add(oldObjRef, oldEntity);
                        }
                    }
                }
                if (oldObjRefToOldEntityMap != null && oldObjRefToOldEntityMap.Count > 0)
                {
                    IDictionary<IObjRef, T> newObjRefToNewEntityMap = new Dictionary<IObjRef, T>();
                    for (int a = interestingEntities.Count; a-- > 0;)
                    {
                        T newEntity = interestingEntities[a];
                        Type newEntityType = ProxyHelper.GetRealType(newEntity.GetType());
                        PrimitiveMember idMember = typeToMetaDataDict[newEntityType].IdMember;
                        Object newEntityId = idMember.GetValue(newEntity, false);

                        IObjRef newObjRef = new ObjRef(newEntityType, ObjRef.PRIMARY_KEY_INDEX, newEntityId, null);
                        newObjRefToNewEntityMap.Add(newObjRef, newEntity);
                    }
                    DictionaryExtension.Loop(oldObjRefToOldEntityMap, delegate(IObjRef objRef, T oldEntity)
                    {
                        T newEntity = DictionaryExtension.ValueOrDefault(newObjRefToNewEntityMap, objRef);
                        if (newEntity == null)
                        {
                            // Nothing to do if current oldEntity has no corresponding newEntity
                            return;
                        }
                        entitiesToAdd.Remove(newEntity);
                        if (!Object.ReferenceEquals(oldEntity, newEntity)
                            && (dataChange.IsLocalSource || !(oldEntity is IDataObject) || !((IDataObject)oldEntity).ToBeUpdated))
                        {
                            entitiesToReplace[oldEntity] = newEntity;
                        }
                        entitiesToRemove.Remove(oldEntity);
                    });
                }
                
                if (mergeModel)
                {
                    for (int a = entities.Count; a-- > 0; )
                    {
                        T item = entities[a];
                        if (entitiesToRemove.Contains(item))
                        {
                            Model.RemoveAt(a);
                            continue;
                        }
                        T replacingItem = DictionaryExtension.ValueOrDefault(entitiesToReplace, item);
                        if (replacingItem != null)
                        {
                            Model.Replace(a, replacingItem);
                            continue;
                        }
                    }
                    IEnumerator<T> enumerator = entitiesToAdd.GetEnumerator();
                    while (enumerator.MoveNext())
                    {
                        T entityToAdd = enumerator.Current;
                        Model.Add(entityToAdd);
                    }

                    if (hasPagedViewModel)
                    {
                        UpdatePagingInformation(pagingResponse);
                    }
                    UpdateAfterDCE();
                }
            });
        }

        protected IFilterDescriptor GetFilterDescriptor()
        {
            if (FilterDescriptorProvider != null)
            {
                return FilterDescriptorProvider.AmbethFilterDescriptor;
            }
            return null;
        }

        protected IList<ISortDescriptor> GetSortDescriptors()
        {
            if (SortDescriptorProvider != null)
            {
                if (SortDescriptorProvider.SortDescriptorList.Count > 0)
                {
                    return SortDescriptorProvider.SortDescriptorList;
                }
            }
            return null;
        }

        protected IPagingRequest GetPagingRequest()
        {
            if (hasPagedViewModel)
            {
                PagingRequest paging = new PagingRequest();
                paging.Number = Model.PageIndex;
                paging.Size = Model.PageSize;
                return paging;
            }
            return null;
        }

        protected virtual Object[] GetContextInformation()
        {
            return EMPTY_CONTEXT;
        }

        protected virtual bool IsRefreshedDataValid(Object[] contextInformation)
        {
            return true;
        }

        protected virtual bool EqualsItems(Type leftRealType, Object leftId, T right, ITypeInfoItem idMember)
        {
            if (right == null)
            {
                return false;
            }
            Type rightRealType = ProxyHelper.GetRealType(right.GetType());
            if (!leftRealType.Equals(rightRealType))
            {
                return false;
            }
            Object rightId = idMember.GetValue(right);
            leftId = ConversionHelper.ConvertValueToType(idMember.RealType, leftId);

            return Object.Equals(leftId, rightId);
        }

        protected virtual bool EqualsItems(Type leftRealType, Object leftId, Type rightRealType, Object rightId)
        {
            if (rightId == null)
            {
                return false;
            }
            if (!leftRealType.Equals(rightRealType))
            {
                return false;
            }
            rightId = ConversionHelper.ConvertValueToType(leftId.GetType(), rightId);
            return Object.Equals(leftId, rightId);
        }
        
        public virtual void OnClientFilterChanged(Object sender, EventArgs e)
        {
            Populate();
        }

        public virtual void Populate()
        {
            var localRequest = new Object();
            lock (currentRequestLock)
            {
                currentRequest = localRequest;
            }
            if (GuiThreadHelper.IsInGuiThread())
            {
                IFilterDescriptor filterDescriptor = GetFilterDescriptor();
                IList<ISortDescriptor> sortDescriptors = GetSortDescriptors();
                IPagingRequest pagingRequest = GetPagingRequest();
                Object[] contextInformation = GetContextInformation();

                Model.IsBusy = true;
                ThreadPool.Queue((GenericViewModel<T> model) =>
                {
                    CacheContext.ExecuteWithCache<Object>(CacheProvider.GetCurrentCache(), delegate()
                    {
                        ConfigureCacheWithEagerLoads(Cache);
                        PopulateAsync(model, filterDescriptor, sortDescriptors, pagingRequest, contextInformation, Cache, localRequest);
                        return null;
                    });
                }, Model);
            }
            else
            {
                IFilterDescriptor filterDescriptor = null;
                IList<ISortDescriptor> sortDescriptors = null;
                IPagingRequest pagingRequest = null;
                Object[] contextInformation = null;
                GuiThreadHelper.InvokeInGuiAndWait(delegate()
                {
                    filterDescriptor = GetFilterDescriptor();
                    sortDescriptors = GetSortDescriptors();
                    pagingRequest = GetPagingRequest();

                    contextInformation = GetContextInformation();

                    Model.IsBusy = true;
                });
                CacheContext.ExecuteWithCache<Object>(CacheProvider.GetCurrentCache(), delegate()
                {
                    ConfigureCacheWithEagerLoads(Cache);
                    PopulateAsync(Model, filterDescriptor, sortDescriptors, pagingRequest, contextInformation, Cache, localRequest);
                    return null;
                });
            }
        }

        protected virtual void PopulateAsync(GenericViewModel<T> model, IFilterDescriptor filterDescriptor, IList<ISortDescriptor> sortDescriptors, IPagingRequest pagingRequest, Object[] contextInformation, ICache cache, Object localRequest)
        {
            lock (currentRequestLock)
            {
                // Early check here, but more important check in the finally-SyncContext.
                // We will not update the ViewModel, if this request is not the current request (hence Populate was recalled
                // since this request was initiated).
                // An example where this is important would be a screen, where the user can enter search criteria and start
                // a corresponding search, while the screen is still loading data from a preceeding request. In this case, the
                // result of the second search could be retrieved before the first one, leading to wrong data in the screen.
                //
                // ToDo: Is there a scenario where the same VMDCC is used with multiple VMs or different Caches?
                //       If so, localRequest and currentRequest must contain the VM and Cache references to compare them!
                if (!Object.ReferenceEquals(localRequest, currentRequest))
                {
                    return;
                }
            }
            IList<T> initialEntities = null;
            IPagingResponse pagingResponse = null;
            try
            {
                if (hasPagedViewModel)
                {
                    pagingResponse = ((IPagingRefresher<T>)Refresher).Populate(filterDescriptor, sortDescriptors, pagingRequest, contextInformation);
                    initialEntities = new List<T>();
                    foreach (Object obj in pagingResponse.Result)
                    {
                        initialEntities.Add((T)obj);
                    }
                }
                else
                {
                    if (filterDescriptor != null || sortDescriptors != null)
                    {
                        contextInformation = new Object[2];
                        contextInformation[0] = filterDescriptor;
                        contextInformation[1] = sortDescriptors;
                    }
                    initialEntities = ((IRefresher<T>)Refresher).Populate(contextInformation);
                }
            }
            catch (Exception e)
            {
                if (Log.ErrorEnabled)
                {
                    Log.Error(e);
                }
            }
            finally
            {
                GuiThreadHelper.InvokeInGui(delegate()
                {
                    lock (currentRequestLock)
                    {
                        if (!Object.ReferenceEquals(localRequest, currentRequest))
                        {
                            return;
                        }
                    }
                    try
                    {
                        if (IsRefreshedDataValid(contextInformation))
                        {
                            if (initialEntities != null)
                            {
                                if (ToBeCreatedOnTop)
                                {
                                    // ToBeCreated entities are never part of the initialEntities, so no doublette check necessary
                                    foreach (T entity in model.Objects)
                                    {
                                        if ((entity is IDataObject) && ((IDataObject)entity).ToBeCreated)
                                        {
                                            initialEntities.Insert(0, entity);
                                        }
                                    }
                                }
                                model.Clear();
                                for (int a = 0, size = initialEntities.Count; a < size; a++)
                                {
                                    T item = initialEntities[a];
                                    model.InsertAt(a, item);
                                }
                            }

                            // Important to update the sorting states (if the provider disables gridviews own sorting mechanism):
                            if (hasPagedViewModel)
                            {
                                UpdatePagingInformation(pagingResponse);
                            }
                            UpdateAfterDCE();
                        }
                    }
                    catch (Exception ex)
                    {
                        if (Log.ErrorEnabled)
                        {
                            Log.Error(ex);
                        }
                    }
                    finally
                    {
                        model.IsBusy = false;
                    }
                });
            }
        }

        public virtual void Update(IList<T> itemsToUpdate, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback)
        {
            foreach (T itemToUpdate in itemsToUpdate)
            {
                Update(itemToUpdate, onErrorCallback, onSuccessCallback);
            }
        }

        public virtual void Delete(IList<T> itemsToDelete, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback)
        {
            foreach (T itemToDelete in itemsToDelete)
            {
                Delete(itemToDelete, onErrorCallback, onSuccessCallback);
            }
        }

        public virtual void Update(T itemToUpdate, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback)
        {
            throw new NotImplementedException();
        }

        public virtual void Delete(T itemToDelete, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback)
        {
            throw new NotImplementedException();
        }

        protected virtual void ConfigureCacheWithEagerLoads(ICache cache)
        {
            if (InitializedRelations != null)
            {
                foreach (KeyValuePair<Type, IList<String>> entry in InitializedRelations)
                {
                    Type entityType = entry.Key;
                    IList<String> relationPaths = entry.Value;

                    foreach (String relationPath in relationPaths)
                    {
                        cache.CascadeLoadPath(entityType, relationPath);
                    }
                }
            }
        }
    }
}
