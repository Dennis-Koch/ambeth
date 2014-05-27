package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.model.IUpdateItem;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.ParamChecker;

public class CUDResultHelper implements IInitializingBean, ICUDResultHelper, ICUDResultExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IObjRefHelper oriHelper;

	protected final ClassExtendableContainer<ICUDResultExtension> extensions = new ClassExtendableContainer<ICUDResultExtension>(
			ICUDResultExtension.class.getSimpleName(), "entityType");

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
		ParamChecker.assertNotNull(oriHelper, "oriHelper");
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public ICUDResult createCUDResult(MergeHandle mergeHandle)
	{
		{
			ILinkedMap<Class<?>, ICUDResultExtension> typeToCudResultExtension = extensions.getExtensions();
			for (Entry<Class<?>, ICUDResultExtension> entry : typeToCudResultExtension)
			{
				entry.getValue().extend(mergeHandle);
			}
		}

		IMap<Object, IList<IUpdateItem>> objToModDict = mergeHandle.objToModDict;
		ISet<Object> objToDeleteSet = mergeHandle.objToDeleteSet;

		List<IPrimitiveUpdateItem> modItemList = new ArrayList<IPrimitiveUpdateItem>();
		List<IRelationUpdateItem> oriModItemList = new ArrayList<IRelationUpdateItem>();

		List<IChangeContainer> allChanges = new ArrayList<IChangeContainer>(objToModDict.size());
		List<Object> originalRefs = new ArrayList<Object>(objToModDict.size());

		for (Object objToDelete : objToDeleteSet)
		{
			IObjRef ori = oriHelper.getCreateObjRef(objToDelete, mergeHandle);
			if (ori == null)
			{
				continue;
			}
			DeleteContainer deleteContainer = new DeleteContainer();
			deleteContainer.setReference(ori);
			allChanges.add(deleteContainer);
			originalRefs.add(objToDelete);
		}
		for (Entry<Object, IList<IUpdateItem>> entry : objToModDict)
		{
			Object obj = entry.getKey();
			List<IUpdateItem> modItems = entry.getValue();

			for (int a = modItems.size(); a-- > 0;)
			{
				IUpdateItem modItem = modItems.get(a);

				if (modItem instanceof IRelationUpdateItem)
				{
					oriModItemList.add((IRelationUpdateItem) modItem);
				}
				else
				{
					modItemList.add((IPrimitiveUpdateItem) modItem);
				}
			}
			IRelationUpdateItem[] childItems = null;
			IPrimitiveUpdateItem[] items = null;
			if (oriModItemList.size() > 0)
			{
				childItems = ListUtil.toArray(IRelationUpdateItem.class, oriModItemList);
				oriModItemList.clear();
			}
			if (modItemList.size() > 0)
			{
				items = ListUtil.toArray(IPrimitiveUpdateItem.class, modItemList);
				modItemList.clear();
			}
			IObjRef ori = oriHelper.getCreateObjRef(obj, mergeHandle);
			originalRefs.add(obj);

			if (ori instanceof IDirectObjRef)
			{
				CreateContainer createContainer = new CreateContainer();

				((IDirectObjRef) ori).setCreateContainerIndex(allChanges.size());

				createContainer.setReference(ori);
				createContainer.setPrimitives(items);
				createContainer.setRelations(childItems);

				allChanges.add(createContainer);
			}
			else
			{
				UpdateContainer updateContainer = new UpdateContainer();
				updateContainer.setReference(ori);
				updateContainer.setPrimitives(items);
				updateContainer.setRelations(childItems);
				allChanges.add(updateContainer);
			}
		}
		return new CUDResult(allChanges, originalRefs);
	}

	@Override
	public void registerCUDResultExtension(ICUDResultExtension cudResultExtension, Class<?> entityType)
	{
		extensions.register(cudResultExtension, entityType);
	}

	@Override
	public void unregisterCUDResultExtension(ICUDResultExtension cudResultExtension, Class<?> entityType)
	{
		extensions.unregister(cudResultExtension, entityType);
	}
}
