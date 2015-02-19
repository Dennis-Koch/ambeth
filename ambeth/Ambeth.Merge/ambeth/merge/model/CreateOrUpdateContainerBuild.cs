using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Merge.Model
{
    public class CreateOrUpdateContainerBuild : AbstractChangeContainer, ICreateOrUpdateContainer
    {
        protected IPrimitiveUpdateItem[] fullPUIs;

        protected readonly HashMap<String, int?> relationNameToIndexMap;

        protected readonly HashMap<String, int?> primitiveNameToIndexMap;

        protected IRelationUpdateItem[] fullRUIs;

        protected int ruiCount, puiCount;

        protected bool isCreate;

        protected readonly ICUDResultHelper cudResultHelper;

        public CreateOrUpdateContainerBuild(bool isCreate, HashMap<String, int?> relationNameToIndexMap, HashMap<String, int?> primitiveNameToIndexMap,
            ICUDResultHelper cudResultHelper)
        {
            this.isCreate = isCreate;
            this.relationNameToIndexMap = relationNameToIndexMap;
            this.primitiveNameToIndexMap = primitiveNameToIndexMap;
            this.cudResultHelper = cudResultHelper;
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
            int? indexR = primitiveNameToIndexMap.Get(pui.MemberName);
            if (!indexR.HasValue)
            {
                throw new Exception("No primitive member " + Reference.RealType.FullName + "." + pui.MemberName + " defined");
            }
            int index = indexR.Value;
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
            int? indexR = relationNameToIndexMap.Get(rui.MemberName);
            if (!indexR.HasValue)
            {
                throw new Exception("No relation member " + Reference.RealType.FullName + "." + rui.MemberName + " defined");
            }
            int index = indexR.Value;
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
            int? indexR = primitiveNameToIndexMap.Get(memberName);
            if (!indexR.HasValue)
            {
                throw new Exception("No primitive member " + Reference.RealType.FullName + "." + memberName + " defined");
            }
            return (PrimitiveUpdateItem)fullPUIs[indexR.Value];
        }

        public RelationUpdateItemBuild FindRelation(String memberName)
        {
            if (fullRUIs == null)
            {
                return null;
            }
            int? indexR = relationNameToIndexMap.Get(memberName);
            if (!indexR.HasValue)
            {
                throw new Exception("No relation member " + Reference.RealType.FullName + "." + memberName + " defined");
            }
            return (RelationUpdateItemBuild)fullRUIs[indexR.Value];
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

        public override void ToString(StringBuilder sb)
	    {
		    if (IsCreate())
		    {
			    sb.Append(typeof(CreateContainer).Name).Append(": ");
		    }
		    else if (IsUpdate())
		    {
			    sb.Append(typeof(UpdateContainer).Name).Append(": ");
		    }
		    else
		    {
			    base.ToString(sb);
			    return;
		    }
		    StringBuilderUtil.AppendPrintable(sb, Reference);
	    }

	    public ICreateOrUpdateContainer Build()
	    {
            if (IsCreate())
		    {
			    CreateContainer cc = new CreateContainer();
			    cc.Reference = Reference;
			    cc.Primitives = cudResultHelper.CompactPUIs(GetFullPUIs(), GetPuiCount());
			    cc.Relations = cudResultHelper.CompactRUIs(GetFullRUIs(), GetRuiCount());
			    return cc;
		    }
            if (IsUpdate())
		    {
			    UpdateContainer uc = new UpdateContainer();
			    uc.Reference = Reference;
			    uc.Primitives = cudResultHelper.CompactPUIs(GetFullPUIs(), GetPuiCount());
			    uc.Relations = cudResultHelper.CompactRUIs(GetFullRUIs(), GetRuiCount());
			    return uc;
		    }
		    throw new Exception("Must never happen");
	    }
    }
}