using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Merge
{
    public class CUDResultHelper : IInitializingBean, ICUDResultHelper, ICUDResultExtendable
    {
        public virtual IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public virtual IObjRefHelper OriHelper { protected get; set; }

        protected readonly IMapExtendableContainer<Type, ICUDResultExtension> extensions = new ClassExtendableContainer<ICUDResultExtension>(typeof(ICUDResultExtension).Name, "entityType");

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
            ParamChecker.AssertNotNull(OriHelper, "OriHelper");
        }

        public ICUDResult CreateCUDResult(MergeHandle mergeHandle)
        {
            ILinkedMap<Type, ICUDResultExtension> typeToCudResultExtension = extensions.GetExtensions();
		    foreach (Entry<Type, ICUDResultExtension> entry in typeToCudResultExtension)
		    {
			    entry.Value.Extend(mergeHandle);
		    }

		    IdentityLinkedMap<Object, IList<IUpdateItem>> objToModDict = mergeHandle.objToModDict;
		    IdentityHashSet<Object> objToDeleteSet = mergeHandle.objToDeleteSet;

		    HashMap<Type, IPrimitiveUpdateItem[]> entityTypeToFullPuis = new HashMap<Type, IPrimitiveUpdateItem[]>();
		    HashMap<Type, IRelationUpdateItem[]> entityTypeToFullRuis = new HashMap<Type, IRelationUpdateItem[]>();

		    List<IChangeContainer> allChanges = new List<IChangeContainer>(objToModDict.Count);
		    List<Object> originalRefs = new List<Object>(objToModDict.Count);

            foreach (Object objToDelete in objToDeleteSet)
            {
                IObjRef ori = OriHelper.GetCreateObjRef(objToDelete, mergeHandle);
                DeleteContainer deleteContainer = new DeleteContainer();
                deleteContainer.Reference = ori;
                allChanges.Add(deleteContainer);
                originalRefs.Add(objToDelete);
            }
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;

            foreach (Entry<Object, IList<IUpdateItem>> entry in objToModDict)
		    {
			    Object obj = entry.Key;
			    IList<IUpdateItem> modItems = entry.Value;

			    IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(obj.GetType());

			    IPrimitiveUpdateItem[] fullPuis = GetEnsureFullPUIs(metaData, entityTypeToFullPuis);
			    IRelationUpdateItem[] fullRuis = GetEnsureFullRUIs(metaData, entityTypeToFullRuis);

			    int puiCount = 0, ruiCount = 0;
			    for (int a = modItems.Count; a-- > 0;)
			    {
				    IUpdateItem modItem = modItems[a];

				    Member member = metaData.GetMemberByName(modItem.MemberName);

				    if (modItem is IRelationUpdateItem)
				    {
					    fullRuis[metaData.GetIndexByRelation(member)] = (IRelationUpdateItem) modItem;
					    ruiCount++;
				    }
				    else
				    {
					    fullPuis[metaData.GetIndexByPrimitive(member)] = (IPrimitiveUpdateItem) modItem;
					    puiCount++;
				    }
			    }

			    IRelationUpdateItem[] ruis = CompactRUIs(fullRuis, ruiCount);
			    IPrimitiveUpdateItem[] puis = CompactPUIs(fullPuis, puiCount);
                IObjRef ori = OriHelper.GetCreateObjRef(obj, mergeHandle);
                originalRefs.Add(obj);

                if (ori is IDirectObjRef)
                {
                    CreateContainer createContainer = new CreateContainer();

                    ((IDirectObjRef)ori).CreateContainerIndex = allChanges.Count;

                    createContainer.Reference = ori;
                    createContainer.Primitives = puis;
                    createContainer.Relations = ruis;

                    allChanges.Add(createContainer);
                }
                else
                {
                    UpdateContainer updateContainer = new UpdateContainer();
                    updateContainer.Reference = ori;
                    updateContainer.Primitives = puis;
                    updateContainer.Relations = ruis;
                    allChanges.Add(updateContainer);
                }
            }
            return new CUDResult(allChanges, originalRefs);
        }

        public IPrimitiveUpdateItem[] GetEnsureFullPUIs(IEntityMetaData metaData, IMap<Type, IPrimitiveUpdateItem[]> entityTypeToFullPuis)
	    {
            IPrimitiveUpdateItem[] fullPuis = entityTypeToFullPuis.Get(metaData.EntityType);
		    if (fullPuis == null)
		    {
			    fullPuis = new IPrimitiveUpdateItem[metaData.PrimitiveMembers.Length];
                entityTypeToFullPuis.Put(metaData.EntityType, fullPuis);
		    }
		    return fullPuis;
	    }

	    public IRelationUpdateItem[] GetEnsureFullRUIs(IEntityMetaData metaData, IMap<Type, IRelationUpdateItem[]> entityTypeToFullRuis)
	    {
            IRelationUpdateItem[] fullRuis = entityTypeToFullRuis.Get(metaData.EntityType);
		    if (fullRuis == null)
		    {
                fullRuis = new IRelationUpdateItem[metaData.RelationMembers.Length];
                entityTypeToFullRuis.Put(metaData.EntityType, fullRuis);
		    }
		    return fullRuis;
	    }

	    public IPrimitiveUpdateItem[] CompactPUIs(IPrimitiveUpdateItem[] fullPUIs, int puiCount)
	    {
		    if (puiCount == 0)
		    {
			    return null;
		    }
		    IPrimitiveUpdateItem[] puis = new IPrimitiveUpdateItem[puiCount];
            for (int a = fullPUIs.Length; a-- > 0; )
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

	    public IRelationUpdateItem[] CompactRUIs(IRelationUpdateItem[] fullRUIs, int ruiCount)
	    {
		    if (ruiCount == 0)
		    {
			    return null;
		    }
		    IRelationUpdateItem[] ruis = new IRelationUpdateItem[ruiCount];
		    for (int a = fullRUIs.Length; a-- > 0;)
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

        public void RegisterCUDResultExtension(ICUDResultExtension cudResultExtension, Type entityType)
        {
            extensions.Register(cudResultExtension, entityType);
        }

        public void UnregisterCUDResultExtension(ICUDResultExtension cudResultExtension, Type entityType)
        {
            extensions.Unregister(cudResultExtension, entityType);
        }
    }
}
