using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;

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

            bool changeOccured = false;
            while (true)
            {
                changeOccured = false;
                foreach (Entry<Type, ICUDResultExtension> entry in typeToCudResultExtension)
                {
                    changeOccured = changeOccured || entry.Value.Extend(mergeHandle);
                }
                if (!changeOccured)
                {
                    break;
                }
            }

            IDictionary<Object, IList<IUpdateItem>> objToModDict = mergeHandle.objToModDict;
            ISet<Object> objToDeleteSet = mergeHandle.objToDeleteSet;

            List<IPrimitiveUpdateItem> modItemList = new List<IPrimitiveUpdateItem>();
            List<IRelationUpdateItem> oriModItemList = new List<IRelationUpdateItem>();

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

            IEnumerator<Object> objEnum = objToModDict.Keys.GetEnumerator();
            while (objEnum.MoveNext())
            {
                Object obj = objEnum.Current;
                Type objType = obj.GetType();

                IList<IUpdateItem> modItems = objToModDict[obj];

                for (int a = modItems.Count; a-- > 0; )
                {
                    IUpdateItem modItem = modItems[a];

                    if (modItem is IRelationUpdateItem)
                    {
                        oriModItemList.Add((IRelationUpdateItem)modItem);
                    }
                    else
                    {
                        modItemList.Add((IPrimitiveUpdateItem)modItem);
                    }
                }
                IRelationUpdateItem[] relations = null;
                IPrimitiveUpdateItem[] primitives = null;
                if (oriModItemList.Count > 0)
                {
                    relations = oriModItemList.ToArray();
                    oriModItemList.Clear();
                }
                if (modItemList.Count > 0)
                {
                    primitives = modItemList.ToArray();
                    modItemList.Clear();
                }
                IObjRef ori = OriHelper.GetCreateObjRef(obj, mergeHandle);
                originalRefs.Add(obj);

                if (ori is IDirectObjRef)
                {
                    CreateContainer createContainer = new CreateContainer();

                    ((IDirectObjRef)ori).CreateContainerIndex = allChanges.Count;

                    createContainer.Reference = ori;
                    createContainer.Primitives = primitives;
                    createContainer.Relations = relations;

                    allChanges.Add(createContainer);
                }
                else
                {
                    UpdateContainer updateContainer = new UpdateContainer();
                    updateContainer.Reference = ori;
                    updateContainer.Primitives = primitives;
                    updateContainer.Relations = relations;
                    allChanges.Add(updateContainer);
                }
            }
            return new CUDResult(allChanges, originalRefs);
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
