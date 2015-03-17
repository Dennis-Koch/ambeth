using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge
{
    public class CUDResultComparer : ICUDResultComparer
    {
        public class CUDResultDiff
        {
            public readonly bool doFullDiff;

            public readonly ICUDResult left;

            public readonly ICUDResult right;

            public readonly IList<IChangeContainer> diffChanges;

            public readonly IList<Object> originalRefs;

            private IChangeContainer leftContainer;

            public String memberName;

            public CreateOrUpdateContainerBuild containerBuild;

            public RelationUpdateItemBuild relationBuild;

            protected bool hasChanges;

            private readonly ICUDResultHelper cudResultHelper;

            private readonly IEntityMetaDataProvider entityMetaDataProvider;

            private HashMap<Type, HashMap<String, int?>> typeToMemberNameToIndexMap;

            private HashMap<Type, HashMap<String, int?>> typeToPrimitiveMemberNameToIndexMap;

            public CUDResultDiff(ICUDResult left, ICUDResult right, bool doFullDiff, ICUDResultHelper cudResultHelper, IEntityMetaDataProvider entityMetaDataProvider)
            {
                this.doFullDiff = doFullDiff;
                this.left = left;
                this.right = right;
                this.cudResultHelper = cudResultHelper;
                this.entityMetaDataProvider = entityMetaDataProvider;
                if (doFullDiff)
                {
                    diffChanges = new List<IChangeContainer>();
                    originalRefs = new List<Object>();
                }
                else
                {
                    diffChanges = EmptyList.Empty<IChangeContainer>();
                    originalRefs = EmptyList.Empty<Object>();
                }
            }

            public void SetHasChanges(bool hasChanges)
            {
                this.hasChanges = hasChanges;
            }

            public bool HasChanges()
            {
                return hasChanges || diffChanges.Count > 0;
            }

            public CreateOrUpdateContainerBuild UpdateContainerBuild()
            {
                if (containerBuild != null)
                {
                    return containerBuild;
                }
                Type entityType = GetLeftContainer().Reference.RealType;
                containerBuild = new CreateOrUpdateContainerBuild(leftContainer is CreateContainer, GetOrCreateRelationMemberNameToIndexMap(entityType),
                        GetOrCreatePrimitiveMemberNameToIndexMap(entityType), cudResultHelper);
                containerBuild.Reference = GetLeftContainer().Reference;
                return containerBuild;
            }

            public RelationUpdateItemBuild UpdateRelationBuild()
            {
                if (relationBuild != null)
                {
                    return relationBuild;
                }
                relationBuild = new RelationUpdateItemBuild(memberName);
                return relationBuild;
            }

            protected HashMap<String, int?> GetOrCreateRelationMemberNameToIndexMap(Type entityType)
            {
                if (typeToMemberNameToIndexMap == null)
                {
                    typeToMemberNameToIndexMap = new HashMap<Type, HashMap<String, int?>>();
                }
                HashMap<String, int?> memberNameToIndexMap = typeToMemberNameToIndexMap.Get(entityType);
                if (memberNameToIndexMap != null)
                {
                    return memberNameToIndexMap;
                }
                IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(entityType);
                RelationMember[] relationMembers = metaData.RelationMembers;
                memberNameToIndexMap = HashMap<String, int?>.Create(relationMembers.Length);
                for (int a = relationMembers.Length; a-- > 0; )
                {
                    memberNameToIndexMap.Put(relationMembers[a].Name, a);
                }
                typeToMemberNameToIndexMap.Put(entityType, memberNameToIndexMap);
                return memberNameToIndexMap;
            }

            protected HashMap<String, int?> GetOrCreatePrimitiveMemberNameToIndexMap(Type entityType)
            {
                if (typeToPrimitiveMemberNameToIndexMap == null)
                {
                    typeToPrimitiveMemberNameToIndexMap = new HashMap<Type, HashMap<String, int?>>();
                }
                HashMap<String, int?> memberNameToIndexMap = typeToPrimitiveMemberNameToIndexMap.Get(entityType);
                if (memberNameToIndexMap != null)
                {
                    return memberNameToIndexMap;
                }
                IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(entityType);
                PrimitiveMember[] primitiveMembers = metaData.PrimitiveMembers;
                memberNameToIndexMap = HashMap<String, int?>.Create(primitiveMembers.Length);
                for (int a = primitiveMembers.Length; a-- > 0; )
                {
                    memberNameToIndexMap.Put(primitiveMembers[a].Name, a);
                }
                typeToPrimitiveMemberNameToIndexMap.Put(entityType, memberNameToIndexMap);
                return memberNameToIndexMap;
            }

            public IChangeContainer GetLeftContainer()
            {
                return leftContainer;
            }

            public void SetLeftContainer(IChangeContainer leftContainer)
            {
                if (leftContainer != null && containerBuild != null)
                {
                    throw new Exception();
                }
                this.leftContainer = leftContainer;
            }

        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICUDResultHelper CudResultHelper { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        protected bool EqualsChangeContainer(CUDResultDiff cudResultDiff, IChangeContainer left, IChangeContainer right)
        {
            if (left.GetType() != right.GetType())
            {
                throw new Exception("Must never happen");
            }
            cudResultDiff.SetLeftContainer(left);
            try
            {
                if (left is CreateContainer)
                {
                    CreateContainer leftCreate = (CreateContainer)left;
                    CreateContainer rightCreate = (CreateContainer)right;
                    bool isEqual = EqualsPUIs(cudResultDiff, leftCreate.Primitives, rightCreate.Primitives);
                    if (!isEqual)
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                    }
                    isEqual &= EqualsRUIs(cudResultDiff, leftCreate.Relations, rightCreate.Relations);
                    if (!isEqual)
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                    }
                    return isEqual;
                }
                if (left is UpdateContainer)
                {
                    UpdateContainer leftUpdate = (UpdateContainer)left;
                    UpdateContainer rightUpdate = (UpdateContainer)right;
                    bool isEqual = EqualsPUIs(cudResultDiff, leftUpdate.Primitives, rightUpdate.Primitives);
                    if (!isEqual)
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                    }
                    isEqual &= EqualsRUIs(cudResultDiff, leftUpdate.Relations, rightUpdate.Relations);
                    if (!isEqual)
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                    }
                    return isEqual;
                }
                // a DeleteContainer is only compared by the reference. But we know that this is already equal since we entered this method
                return true;
            }
            finally
            {
                cudResultDiff.SetLeftContainer(null);
            }
        }

        public bool EqualsCUDResult(ICUDResult left, ICUDResult right)
        {
            CUDResultDiff diff = new CUDResultDiff(left, right, false, CudResultHelper, EntityMetaDataProvider);
            return EqualsCUDResult(diff);
        }

        public ICUDResult DiffCUDResult(ICUDResult left, ICUDResult right)
        {
            CUDResultDiff diff = new CUDResultDiff(left, right, true, CudResultHelper, EntityMetaDataProvider);
            EqualsCUDResult(diff);

            if (!diff.HasChanges())
            {
                return null; // null means empty diff
            }
            IList<IChangeContainer> diffChanges = diff.diffChanges;
            for (int a = diffChanges.Count; a-- > 0; )
            {
                IChangeContainer changeContainer = diffChanges[a];
                if (!(changeContainer is CreateOrUpdateContainerBuild))
                {
                    continue;
                }
                diffChanges[a] = ((CreateOrUpdateContainerBuild)changeContainer).Build();
            }
            return new CUDResult(diffChanges, diff.originalRefs);
        }

        protected bool EqualsCUDResult(CUDResultDiff cudResultDiff)
        {
            ICUDResult left = cudResultDiff.left;
            ICUDResult right = cudResultDiff.right;
            IList<Object> leftRefs = left.GetOriginalRefs();
            IList<Object> rightRefs = right.GetOriginalRefs();
            if (leftRefs.Count != rightRefs.Count)
            {
                if (!cudResultDiff.doFullDiff)
                {
                    return false;
                }
            }
            IList<IChangeContainer> leftChanges = left.AllChanges;
            IList<IChangeContainer> rightChanges = right.AllChanges;
            IdentityHashMap<Object, int?> rightMap = IdentityHashMap<Object, int?>.Create(rightRefs.Count);
            for (int a = rightRefs.Count; a-- > 0; )
            {
                rightMap.Put(rightRefs[a], a);
            }
            for (int a = leftRefs.Count; a-- > 0; )
            {
                Object leftEntity = leftRefs[a];
                int? rightIndex = rightMap.Remove(leftEntity);
                if (!rightIndex.HasValue)
                {
                    if (!cudResultDiff.doFullDiff)
                    {
                        return false;
                    }
                    cudResultDiff.diffChanges.Add(leftChanges[a]);
                    cudResultDiff.originalRefs.Add(leftEntity);
                    continue;
                }
                if (!EqualsChangeContainer(cudResultDiff, leftChanges[a], rightChanges[rightIndex.Value]))
                {
                    if (!cudResultDiff.doFullDiff)
                    {
                        if (cudResultDiff.containerBuild != null)
                        {
                            throw new Exception();
                        }
                        return false;
                    }
                    cudResultDiff.diffChanges.Add(cudResultDiff.containerBuild);
                    cudResultDiff.originalRefs.Add(rightRefs[rightIndex.Value]);
                    cudResultDiff.containerBuild = null;
                }
                else if (cudResultDiff.containerBuild != null)
                {
                    throw new Exception();
                }
            }
            if (rightMap.Count == 0)
            {
                return true;
            }
            foreach (Entry<Object, int> entry in rightMap)
            {
                Object rightRef = entry.Key;
                int rightIndex = entry.Value;
                IChangeContainer rightChange = rightChanges[rightIndex];
                cudResultDiff.diffChanges.Add(rightChange);
                cudResultDiff.originalRefs.Add(rightRef);
            }
            return false;
        }

        protected bool EqualsPUIs(CUDResultDiff cudResultDiff, IPrimitiveUpdateItem[] left, IPrimitiveUpdateItem[] right)
        {
            if (left == null || left.Length == 0)
            {
                if (right == null || right.Length == 0)
                {
                    return true;
                }
                if (!cudResultDiff.doFullDiff)
                {
                    return false;
                }
                CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
                foreach (IPrimitiveUpdateItem rightPui in right)
                {
                    containerBuild.AddPrimitive(rightPui);
                }
                return false;
            }
            if (right == null || right.Length == 0)
            {
                throw new Exception("Must never happen");
            }
            if (left.Length != right.Length)
            {
                if (!cudResultDiff.doFullDiff)
                {
                    return false;
                }
                int leftIndex = left.Length - 1;
                for (int rightIndex = right.Length; rightIndex-- > 0; )
                {
                    IPrimitiveUpdateItem leftPui = leftIndex >= 0 ? left[leftIndex] : null;
                    IPrimitiveUpdateItem rightPui = right[rightIndex];
                    if (leftPui == null || !leftPui.MemberName.Equals(rightPui.MemberName))
                    {
                        CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
                        containerBuild.AddPrimitive(rightPui);
                        continue;
                    }
                    if (!EqualsPUI(cudResultDiff, leftPui, rightPui))
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                        CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
                        containerBuild.AddPrimitive(rightPui);
                    }
                    leftIndex--;
                }
                return false;
            }
            bool isEqual = true;
            for (int a = left.Length; a-- > 0; )
            {
                IPrimitiveUpdateItem rightPui = right[a];
                if (!EqualsPUI(cudResultDiff, left[a], rightPui))
                {
                    if (!cudResultDiff.doFullDiff)
                    {
                        return false;
                    }
                    CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
                    containerBuild.AddPrimitive(rightPui);
                    isEqual = false;
                }
            }
            return isEqual;
        }

        protected bool EqualsPUI(CUDResultDiff cudResultDiff, IPrimitiveUpdateItem left, IPrimitiveUpdateItem right)
        {
            if (Object.Equals(left.NewValue, right.NewValue))
            {
                return true;
            }
            if (!cudResultDiff.doFullDiff)
            {
                return false;
            }
            CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
            containerBuild.AddPrimitive(right);
            return false;
        }

        protected bool EqualsRUIs(CUDResultDiff cudResultDiff, IRelationUpdateItem[] left, IRelationUpdateItem[] right)
        {
            if (left == null || left.Length == 0)
            {
                if (right == null || right.Length == 0)
                {
                    return true;
                }
                if (cudResultDiff.doFullDiff)
                {
                    CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
                    foreach (IRelationUpdateItem rightRui in right)
                    {
                        containerBuild.AddRelation(rightRui);
                    }
                }
                return false;
            }
            if (right == null || right.Length == 0)
            {
                throw new Exception("Must never happen");
            }
            if (left.Length != right.Length)
            {
                if (!cudResultDiff.doFullDiff)
                {
                    return false;
                }
                int leftIndex = left.Length - 1;
                for (int rightIndex = right.Length; rightIndex-- > 0; )
                {
                    IRelationUpdateItem leftRui = leftIndex >= 0 ? left[leftIndex] : null;
                    IRelationUpdateItem rightRui = right[rightIndex];
                    if (leftRui == null || !leftRui.MemberName.Equals(rightRui.MemberName))
                    {
                        CreateOrUpdateContainerBuild containerBuild = cudResultDiff.UpdateContainerBuild();
                        containerBuild.AddRelation(rightRui);
                        continue;
                    }
                    if (!EqualsRUI(cudResultDiff, leftRui, rightRui))
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                    }
                    leftIndex--;
                }
                return false;
            }
            bool isEqual = true;
            for (int a = left.Length; a-- > 0; )
            {
                IRelationUpdateItem rightPui = right[a];
                if (!EqualsRUI(cudResultDiff, left[a], rightPui))
                {
                    if (!cudResultDiff.doFullDiff)
                    {
                        return false;
                    }
                    isEqual = false;
                }
            }
            return isEqual;
        }

        protected bool EqualsRUI(CUDResultDiff cudResultDiff, IRelationUpdateItem left, IRelationUpdateItem right)
        {
            // we do NOT have to check each relational ObjRef because IF an objRef is in the scope it must not be removed afterwards
            // so we know by design that the arrays can only grow

            try
            {
                IObjRef[] leftORIs = left.AddedORIs;
                IObjRef[] rightORIs = right.AddedORIs;

                if (leftORIs == null)
                {
                    if (rightORIs != null)
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                        RelationUpdateItemBuild relationBuild = cudResultDiff.UpdateRelationBuild();
                        relationBuild.AddObjRefs(rightORIs);
                    }
                }
                else if (rightORIs == null)
                {
                    throw new Exception("Must never happen");
                }
                else if (leftORIs.Length != rightORIs.Length)
                {
                    if (!cudResultDiff.doFullDiff)
                    {
                        return false;
                    }
                    throw new Exception("Not yet implemented");
                }
                leftORIs = left.RemovedORIs;
                rightORIs = right.RemovedORIs;
                if (leftORIs == null)
                {
                    if (rightORIs != null)
                    {
                        if (!cudResultDiff.doFullDiff)
                        {
                            return false;
                        }
                        RelationUpdateItemBuild relationBuild = cudResultDiff.UpdateRelationBuild();
                        relationBuild.RemoveObjRefs(rightORIs);
                    }
                }
                else if (rightORIs == null)
                {
                    throw new Exception("Must never happen");
                }
                else if (leftORIs.Length != rightORIs.Length)
                {
                    if (!cudResultDiff.doFullDiff)
                    {
                        return false;
                    }
                    throw new Exception("Not yet implemented");
                }
                return true;
            }
            finally
            {
                cudResultDiff.relationBuild = null;
            }
        }
    }
}