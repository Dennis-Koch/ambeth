package com.koch.ambeth.merge;

import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.IUpdateItem;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;

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
		ILinkedMap<Class<?>, ICUDResultExtension> typeToCudResultExtension = extensions.getExtensions();
		for (Entry<Class<?>, ICUDResultExtension> entry : typeToCudResultExtension)
		{
			entry.getValue().extend(mergeHandle);
		}

		IdentityLinkedMap<Object, IList<IUpdateItem>> objToModDict = mergeHandle.objToModDict;
		IdentityHashSet<Object> objToDeleteSet = mergeHandle.objToDeleteSet;

		HashMap<Class<?>, IPrimitiveUpdateItem[]> entityTypeToFullPuis = new HashMap<Class<?>, IPrimitiveUpdateItem[]>();
		HashMap<Class<?>, IRelationUpdateItem[]> entityTypeToFullRuis = new HashMap<Class<?>, IRelationUpdateItem[]>();

		ArrayList<IChangeContainer> allChanges = new ArrayList<IChangeContainer>(objToModDict.size());
		ArrayList<Object> originalRefs = new ArrayList<Object>(objToModDict.size());

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
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		for (Entry<Object, IList<IUpdateItem>> entry : objToModDict)
		{
			Object obj = entry.getKey();
			List<IUpdateItem> modItems = entry.getValue();

			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());

			IPrimitiveUpdateItem[] fullPuis = getEnsureFullPUIs(metaData, entityTypeToFullPuis);
			IRelationUpdateItem[] fullRuis = getEnsureFullRUIs(metaData, entityTypeToFullRuis);

			int puiCount = 0, ruiCount = 0;
			for (int a = modItems.size(); a-- > 0;)
			{
				IUpdateItem modItem = modItems.get(a);

				Member member = metaData.getMemberByName(modItem.getMemberName());

				if (modItem instanceof IRelationUpdateItem)
				{
					fullRuis[metaData.getIndexByRelation(member)] = (IRelationUpdateItem) modItem;
					ruiCount++;
				}
				else
				{
					fullPuis[metaData.getIndexByPrimitive(member)] = (IPrimitiveUpdateItem) modItem;
					puiCount++;
				}
			}

			IRelationUpdateItem[] ruis = compactRUIs(fullRuis, ruiCount);
			IPrimitiveUpdateItem[] puis = compactPUIs(fullPuis, puiCount);
			IObjRef ori = oriHelper.getCreateObjRef(obj, mergeHandle);
			originalRefs.add(obj);

			if (ori instanceof IDirectObjRef)
			{
				CreateContainer createContainer = new CreateContainer();

				((IDirectObjRef) ori).setCreateContainerIndex(allChanges.size());

				createContainer.setReference(ori);
				createContainer.setPrimitives(puis);
				createContainer.setRelations(ruis);

				allChanges.add(createContainer);
			}
			else
			{
				UpdateContainer updateContainer = new UpdateContainer();
				updateContainer.setReference(ori);
				updateContainer.setPrimitives(puis);
				updateContainer.setRelations(ruis);
				allChanges.add(updateContainer);
			}
		}
		return new CUDResult(allChanges, originalRefs);
	}

	@Override
	public IPrimitiveUpdateItem[] getEnsureFullPUIs(IEntityMetaData metaData, IMap<Class<?>, IPrimitiveUpdateItem[]> entityTypeToFullPuis)
	{
		IPrimitiveUpdateItem[] fullPuis = entityTypeToFullPuis.get(metaData.getEntityType());
		if (fullPuis == null)
		{
			fullPuis = new IPrimitiveUpdateItem[metaData.getPrimitiveMembers().length];
			entityTypeToFullPuis.put(metaData.getEntityType(), fullPuis);
		}
		return fullPuis;
	}

	@Override
	public IRelationUpdateItem[] getEnsureFullRUIs(IEntityMetaData metaData, IMap<Class<?>, IRelationUpdateItem[]> entityTypeToFullRuis)
	{
		IRelationUpdateItem[] fullRuis = entityTypeToFullRuis.get(metaData.getEntityType());
		if (fullRuis == null)
		{
			fullRuis = new IRelationUpdateItem[metaData.getRelationMembers().length];
			entityTypeToFullRuis.put(metaData.getEntityType(), fullRuis);
		}
		return fullRuis;
	}

	@Override
	public IPrimitiveUpdateItem[] compactPUIs(IPrimitiveUpdateItem[] fullPUIs, int puiCount)
	{
		if (puiCount == 0)
		{
			return null;
		}
		IPrimitiveUpdateItem[] puis = new IPrimitiveUpdateItem[puiCount];
		for (int a = fullPUIs.length; a-- > 0;)
		{
			IPrimitiveUpdateItem pui = fullPUIs[a];
			if (pui == null)
			{
				continue;
			}
			fullPUIs[a] = null;
			puis[--puiCount] = pui;
		}
		return puis;
	}

	@Override
	public IRelationUpdateItem[] compactRUIs(IRelationUpdateItem[] fullRUIs, int ruiCount)
	{
		if (ruiCount == 0)
		{
			return null;
		}
		IRelationUpdateItem[] ruis = new IRelationUpdateItem[ruiCount];
		for (int a = fullRUIs.length; a-- > 0;)
		{
			IRelationUpdateItem rui = fullRUIs[a];
			if (rui == null)
			{
				continue;
			}
			fullRUIs[a] = null;
			ruis[--ruiCount] = rui;
		}
		return ruis;
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
