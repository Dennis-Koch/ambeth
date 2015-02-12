using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using System;

namespace De.Osthus.Ambeth.Merge.Model
{
    public class CreateOrUpdateContainerBuild : AbstractChangeContainer, ICreateOrUpdateContainer
    {
        protected IPrimitiveUpdateItem[] fullPUIs;

        protected readonly HashMap<String, int> relationNameToIndexMap;

        protected readonly HashMap<String, int> primitiveNameToIndexMap;

        protected IRelationUpdateItem[] fullRUIs;

        protected int ruiCount, puiCount;

        protected bool isCreate;

        public CreateOrUpdateContainerBuild(bool isCreate, HashMap<String, int> relationNameToIndexMap, HashMap<String, int> primitiveNameToIndexMap)
        {
            this.isCreate = isCreate;
            this.relationNameToIndexMap = relationNameToIndexMap;
            this.primitiveNameToIndexMap = primitiveNameToIndexMap;
        }

        public bool IsCreate()
        {
            return isCreate;
        }

        public bool IsUpdate()
        {
            return !IsCreate();
        }

        public IPrimitiveUpdateItem[] GetFullPUIs()
        {
            return fullPUIs;
        }

        public IRelationUpdateItem[] GetFullRUIs()
        {
            return fullRUIs;
        }

        public void AddPrimitive(IPrimitiveUpdateItem pui)
        {
            IPrimitiveUpdateItem[] fullPUIs = this.fullPUIs;
            if (fullPUIs == null)
            {
                fullPUIs = new IPrimitiveUpdateItem[primitiveNameToIndexMap.Count];
                this.fullPUIs = fullPUIs;
            }
            int index = primitiveNameToIndexMap.Get(pui.MemberName);
            if (fullPUIs[index] == null)
            {
                puiCount++;
            }
            fullPUIs[index] = pui;
        }

        public void AddRelation(IRelationUpdateItem rui)
        {
            IRelationUpdateItem[] fullRUIs = this.fullRUIs;
            if (fullRUIs == null)
            {
                fullRUIs = new IRelationUpdateItem[relationNameToIndexMap.Count];
                this.fullRUIs = fullRUIs;
            }
            int index = relationNameToIndexMap.Get(rui.MemberName);
            if (fullRUIs[index] == null)
            {
                ruiCount++;
            }
            fullRUIs[index] = rui;
        }

        public PrimitiveUpdateItem FindPrimitive(String memberName)
        {
            if (fullPUIs == null)
            {
                return null;
            }
            return (PrimitiveUpdateItem)fullPUIs[primitiveNameToIndexMap.Get(memberName)];
        }

        public RelationUpdateItemBuild FindRelation(String memberName)
        {
            if (fullRUIs == null)
            {
                return null;
            }
            return (RelationUpdateItemBuild)fullRUIs[relationNameToIndexMap.Get(memberName)];
        }

        public PrimitiveUpdateItem EnsurePrimitive(String memberName)
        {
            PrimitiveUpdateItem pui = FindPrimitive(memberName);
            if (pui != null)
            {
                return pui;
            }
            pui = new PrimitiveUpdateItem();
            pui.MemberName = memberName;
            AddPrimitive(pui);
            return pui;
        }

        public RelationUpdateItemBuild EsureRelation(String memberName)
        {
            RelationUpdateItemBuild rui = FindRelation(memberName);
            if (rui != null)
            {
                return rui;
            }
            rui = new RelationUpdateItemBuild(memberName);
            AddRelation(rui);
            return rui;
        }

        public int GetPuiCount()
        {
            return puiCount;
        }

        public int GetRuiCount()
        {
            return ruiCount;
        }
    }
}