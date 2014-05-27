package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.OriCollection;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.service.IMergeServiceExtendable;
import de.osthus.ambeth.util.ParamChecker;

public class MergeServiceRegistry implements IMergeService, IMergeServiceExtendable, IInitializingBean
{
	@LogInstance
	private ILogger log;

	public static class MergeOperation
	{
		protected IMergeService mergeService;

		protected IList<IChangeContainer> changeContainer;

		public void setMergeService(IMergeService mergeService)
		{
			this.mergeService = mergeService;
		}

		public IMergeService getMergeService()
		{
			return mergeService;
		}

		public void setChangeContainer(IList<IChangeContainer> changeContainer)
		{
			this.changeContainer = changeContainer;
		}

		public IList<IChangeContainer> getChangeContainer()
		{
			return changeContainer;
		}

	}

	protected final Lock writeLock = new ReentrantLock();;

	protected final ClassExtendableContainer<IMergeService> typeToMergeServiceMap = new ClassExtendableContainer<IMergeService>("elementHandler", "type");

	protected IMergeService defaultMergeService;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IMergeController mergeController;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertParamNotNull(log, "Log");
		ParamChecker.assertParamNotNull(mergeController, "MergeController");
		if (defaultMergeService == this)
		{
			throw new IllegalArgumentException("Property 'DefaultMergeService' is injected with 'this' which is not supported by this bean");
		}
	}

	public void setDefaultMergeService(IMergeService defaultMergeService)
	{
		this.defaultMergeService = defaultMergeService;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setMergeController(IMergeController mergeController)
	{
		this.mergeController = mergeController;
	}

	@Override
	public void registerMergeService(IMergeService mergeService, Class<?> handledType)
	{
		ParamChecker.assertParamNotNull(mergeService, "mergeService");
		ParamChecker.assertParamNotNull(handledType, "handledType");

		writeLock.lock();
		try
		{
			IMergeService registered = typeToMergeServiceMap.getExtension(handledType);
			if (registered == null)
			{
				this.typeToMergeServiceMap.register(mergeService, handledType);
			}
			else if (registered.equals(mergeService))
			{
				if (log.isInfoEnabled())
				{
					log.info("Duplicate registration of same service object for " + handledType);
				}
			}
			else
			{
				if (log.isInfoEnabled())
				{
					log.info("There is already a CacheService mapped to " + handledType);
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterMergeService(IMergeService mergeService, Class<?> handledType)
	{
		ParamChecker.assertParamNotNull(mergeService, "mergeService");
		ParamChecker.assertParamNotNull(handledType, "handledType");

		writeLock.lock();
		try
		{
			this.typeToMergeServiceMap.unregister(mergeService, handledType);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		ParamChecker.assertParamNotNull(cudResult, "cudResult");

		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		List<Object> originalRefs = cudResult.getOriginalRefs();

		IMap<IChangeContainer, Integer> changeToChangeIndexDict = new IdentityHashMap<IChangeContainer, Integer>();
		for (int a = allChanges.size(); a-- > 0;)
		{
			changeToChangeIndexDict.put(allChanges.get(a), a);
		}

		IMap<Class<?>, IList<IChangeContainer>> sortedChanges = bucketSortChanges(allChanges);
		IList<MergeOperation> mergeOperationSequence = createMergeOperationSequence(sortedChanges);

		OriCollection oriCollection = new OriCollection();

		IObjRef[] objRefs = new IObjRef[allChanges.size()];

		for (int a = 0, size = mergeOperationSequence.size(); a < size; a++)
		{
			MergeOperation mergeOperation = mergeOperationSequence.get(a);
			IMergeService mergeService = mergeOperation.getMergeService();
			IList<IChangeContainer> changesForMergeService = mergeOperation.getChangeContainer();

			Object[] msOriginalRefs = new Object[changesForMergeService.size()];
			for (int b = changesForMergeService.size(); b-- > 0;)
			{
				int index = changeToChangeIndexDict.get(changesForMergeService.get(b));
				if (originalRefs != null)
				{
					msOriginalRefs[b] = originalRefs.get(index);
				}
			}
			CUDResult msCudResult = new CUDResult(changesForMergeService, new ArrayList<Object>(msOriginalRefs));
			IOriCollection msOriCollection = mergeService.merge(msCudResult, methodDescription);

			postProcessOriCollection(msCudResult, msOriCollection);

			List<IObjRef> allChangeORIs = msOriCollection.getAllChangeORIs();
			for (int b = changesForMergeService.size(); b-- > 0;)
			{
				int index = changeToChangeIndexDict.get(changesForMergeService.get(b));
				objRefs[index] = allChangeORIs.get(b);

				// Set original ref to null in order to suppress a post-processing in a potentially calling IMergeProcess
				if (originalRefs != null)
				{
					originalRefs.set(b, null);
				}
			}
		}
		oriCollection.setAllChangeORIs(new ArrayList<IObjRef>(objRefs));

		// TODO DCE must be fired HERE <---
		return oriCollection;
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		return defaultMergeService.getMetaData(entityTypes);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return defaultMergeService.getValueObjectConfig(valueType);
	}

	protected IMergeService getServiceForType(Class<?> type)
	{
		if (type == null)
		{
			return null;
		}
		IMergeService mergeService = typeToMergeServiceMap.getExtension(type);
		if (mergeService == null)
		{
			if (defaultMergeService != null)
			{
				mergeService = defaultMergeService;
			}
			else
			{
				throw new IllegalArgumentException("No merge service found to handle entity type '" + type.getName() + "'");
			}
		}

		return mergeService;
	}

	protected IMap<Class<?>, IList<IChangeContainer>> bucketSortChanges(List<IChangeContainer> allChanges)
	{
		IMap<Class<?>, IList<IChangeContainer>> sortedChanges = new HashMap<Class<?>, IList<IChangeContainer>>();

		for (int i = allChanges.size(); i-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(i);
			IObjRef objRef = changeContainer.getReference();
			Class<?> type = objRef.getRealType();
			IList<IChangeContainer> changeContainers = sortedChanges.get(type);
			if (changeContainers == null)
			{
				changeContainers = new ArrayList<IChangeContainer>();
				if (!sortedChanges.putIfNotExists(type, changeContainers))
				{
					throw new IllegalStateException("Key already exists " + type);
				}
			}
			changeContainers.add(changeContainer);
		}
		return sortedChanges;
	}

	protected IList<MergeOperation> createMergeOperationSequence(IMap<Class<?>, IList<IChangeContainer>> sortedChanges)
	{
		Class<?>[] entityPersistOrder = entityMetaDataProvider.getEntityPersistOrder();
		final IList<MergeOperation> mergeOperations = new ArrayList<MergeOperation>();

		if (entityPersistOrder != null)
		{
			for (int a = entityPersistOrder.length; a-- > 0;)
			{
				Class<?> orderedEntityType = entityPersistOrder[a];
				IList<IChangeContainer> changes = sortedChanges.get(orderedEntityType);
				if (changes == null)
				{
					// No changes of current type found. Nothing to do here
					continue;
				}
				IList<IChangeContainer> removes = new ArrayList<IChangeContainer>(changes.size());
				IList<IChangeContainer> insertsAndUpdates = new ArrayList<IChangeContainer>(changes.size());
				for (int b = changes.size(); b-- > 0;)
				{
					IChangeContainer change = changes.get(b);
					if (change instanceof DeleteContainer)
					{
						removes.add(change);
					}
					else
					{
						insertsAndUpdates.add(change);
					}
				}
				if (removes.size() == 0)
				{
					// Nothing to do. Ordering is not necessary here
					continue;
				}
				if (insertsAndUpdates.size() == 0)
				{
					sortedChanges.remove(orderedEntityType);
				}
				else
				{
					sortedChanges.put(orderedEntityType, insertsAndUpdates);
				}
				IMergeService mergeService = getServiceForType(orderedEntityType);
				MergeOperation mergeOperation = new MergeOperation();
				mergeOperation.setMergeService(mergeService);
				mergeOperation.setChangeContainer(removes);

				mergeOperations.add(mergeOperation);
			}
			for (int a = 0, size = entityPersistOrder.length; a < size; a++)
			{
				Class<?> orderedEntityType = entityPersistOrder[a];
				IList<IChangeContainer> changes = sortedChanges.get(orderedEntityType);
				if (changes == null)
				{
					// No changes of current type found. Nothing to do here
					continue;
				}
				boolean containsNew = false;
				for (int b = changes.size(); b-- > 0;)
				{
					if (changes.get(b).getReference().getId() == null)
					{
						containsNew = true;
						break;
					}
				}
				if (!containsNew)
				{
					// Nothing to do. Ordering is not necessary here
					continue;
				}
				// Remove batch of changes where at least 1 new entity occured
				// and
				// this type of entity has to be inserted in a global order
				sortedChanges.remove(orderedEntityType);
				IMergeService mergeService = getServiceForType(orderedEntityType);
				MergeOperation mergeOperation = new MergeOperation();
				mergeOperation.setMergeService(mergeService);
				mergeOperation.setChangeContainer(changes);

				mergeOperations.add(mergeOperation);
			}
		}

		// Everything which is left in the sortedChanges map can be merged
		// without global order, so batch together as much as possible

		for (Entry<Class<?>, IList<IChangeContainer>> entry : sortedChanges)
		{
			Class<?> type = entry.getKey();
			IList<IChangeContainer> unorderedChanges = entry.getValue();
			IMergeService mergeService = getServiceForType(type);

			boolean cont = false;
			for (MergeOperation existingMergeOperation : mergeOperations)
			{
				if (existingMergeOperation.getMergeService() == mergeService)
				{
					IList<IChangeContainer> orderedChanges = existingMergeOperation.getChangeContainer();
					for (int b = unorderedChanges.size(); b-- > 0;)
					{
						orderedChanges.add(unorderedChanges.get(b));
					}
					cont = true;
					break;
				}
			}
			if (cont)
			{
				continue;
			}
			MergeOperation mergeOperation = new MergeOperation();
			mergeOperation.setMergeService(mergeService);
			mergeOperation.setChangeContainer(unorderedChanges);

			mergeOperations.add(mergeOperation);
		}
		return mergeOperations;
	}

	protected void postProcessOriCollection(ICUDResult cudResult, IOriCollection oriCollection)
	{
		mergeController.applyChangesToOriginals(cudResult.getOriginalRefs(), oriCollection.getAllChangeORIs(), oriCollection.getChangedOn(),
				oriCollection.getChangedBy());
	}
}
