package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
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
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.util.ParamChecker;

public class MergeServiceRegistry implements IMergeService, IMergeServiceExtensionExtendable
{
	public static class MergeOperation
	{
		protected IMergeServiceExtension mergeServiceExtension;

		protected IList<IChangeContainer> changeContainer;

		public void setMergeServiceExtension(IMergeServiceExtension mergeServiceExtension)
		{
			this.mergeServiceExtension = mergeServiceExtension;
		}

		public IMergeServiceExtension getMergeServiceExtension()
		{
			return mergeServiceExtension;
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

	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IMergeController mergeController;

	protected final ClassExtendableContainer<IMergeServiceExtension> mergeServiceExtensions = new ClassExtendableContainer<IMergeServiceExtension>(
			"mergeServiceExtension", "entityType");

	@Override
	public void registerMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType)
	{
		mergeServiceExtensions.register(mergeServiceExtension, entityType);
	}

	@Override
	public void unregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType)
	{
		mergeServiceExtensions.unregister(mergeServiceExtension, entityType);
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
			IMergeServiceExtension mergeServiceExtension = mergeOperation.getMergeServiceExtension();
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
			IOriCollection msOriCollection = mergeServiceExtension.merge(msCudResult, methodDescription);

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
		IdentityHashMap<IMergeServiceExtension, List<Class<?>>> mseToEntityTypes = new IdentityHashMap<IMergeServiceExtension, List<Class<?>>>();

		for (int a = entityTypes.size(); a-- > 0;)
		{
			Class<?> entityType = entityTypes.get(a);
			IMergeServiceExtension mergeServiceExtension = mergeServiceExtensions.getExtension(entityType);
			if (mergeServiceExtension == null)
			{
				throw new IllegalArgumentException("No " + IMergeServiceExtension.class.getName() + " registered for type '" + entityType.getName() + "'");
			}
			List<Class<?>> groupedEntityTypes = mseToEntityTypes.get(mergeServiceExtension);
			if (groupedEntityTypes == null)
			{
				groupedEntityTypes = new ArrayList<Class<?>>();
				mseToEntityTypes.put(mergeServiceExtension, groupedEntityTypes);
			}
			groupedEntityTypes.add(entityType);
		}
		ArrayList<IEntityMetaData> metaDataResult = new ArrayList<IEntityMetaData>(entityTypes.size());
		for (Entry<IMergeServiceExtension, List<Class<?>>> entry : mseToEntityTypes)
		{
			List<IEntityMetaData> groupedMetaData = entry.getKey().getMetaData(entry.getValue());
			metaDataResult.addAll(groupedMetaData);
		}
		return metaDataResult;
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return entityMetaDataProvider.getValueObjectConfig(valueType);
	}

	protected IMergeServiceExtension getServiceForType(Class<?> type)
	{
		return getServiceForType(type, false);
	}

	protected IMergeServiceExtension getServiceForType(Class<?> type, boolean tryOnly)
	{
		if (type == null)
		{
			return null;
		}
		IMergeServiceExtension mse = mergeServiceExtensions.getExtension(type);
		if (mse == null && !tryOnly)
		{
			throw new IllegalArgumentException("No " + IMergeServiceExtension.class.getSimpleName() + " found to handle entity type '" + type.getName() + "'");
		}
		return mse;
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
				IMergeServiceExtension mergeServiceExtension = getServiceForType(orderedEntityType);
				MergeOperation mergeOperation = new MergeOperation();
				mergeOperation.setMergeServiceExtension(mergeServiceExtension);
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
				IMergeServiceExtension mergeServiceExtension = getServiceForType(orderedEntityType);
				MergeOperation mergeOperation = new MergeOperation();
				mergeOperation.setMergeServiceExtension(mergeServiceExtension);
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
			IMergeServiceExtension mergeServiceExtension = getServiceForType(type, true);

			if (mergeServiceExtension == null)
			{
				continue;
			}
			boolean cont = false;
			for (MergeOperation existingMergeOperation : mergeOperations)
			{
				if (existingMergeOperation.getMergeServiceExtension() == mergeServiceExtension)
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
			mergeOperation.setMergeServiceExtension(mergeServiceExtension);
			mergeOperation.setChangeContainer(unorderedChanges);

			mergeOperations.add(mergeOperation);
		}
		return mergeOperations;
	}

	protected void postProcessOriCollection(final ICUDResult cudResult, final IOriCollection oriCollection)
	{
		guiThreadHelper.invokeInGuiAndWait(new IBackgroundWorkerDelegate()
		{

			@Override
			public void invoke() throws Throwable
			{
				mergeController.applyChangesToOriginals(cudResult.getOriginalRefs(), oriCollection.getAllChangeORIs(), oriCollection.getChangedOn(),
						oriCollection.getChangedBy());
			}
		});
	}
}
