package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.merge.transfer.AbstractChangeContainer;
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;

public class CreateOrUpdateContainerBuild extends AbstractChangeContainer implements ICreateOrUpdateContainer
{
	protected IPrimitiveUpdateItem[] fullPUIs;

	protected final HashMap<String, Integer> relationNameToIndexMap;

	protected final HashMap<String, Integer> primitiveNameToIndexMap;

	protected IRelationUpdateItem[] fullRUIs;

	protected int ruiCount, puiCount;

	protected boolean isCreate;

	public CreateOrUpdateContainerBuild(boolean isCreate, HashMap<String, Integer> relationNameToIndexMap, HashMap<String, Integer> primitiveNameToIndexMap)
	{
		this.isCreate = isCreate;
		this.relationNameToIndexMap = relationNameToIndexMap;
		this.primitiveNameToIndexMap = primitiveNameToIndexMap;
	}

	public boolean isCreate()
	{
		return isCreate;
	}

	public boolean isUpdate()
	{
		return !isCreate();
	}

	@Override
	public IPrimitiveUpdateItem[] getFullPUIs()
	{
		return fullPUIs;
	}

	@Override
	public IRelationUpdateItem[] getFullRUIs()
	{
		return fullRUIs;
	}

	public void addPrimitive(IPrimitiveUpdateItem pui)
	{
		IPrimitiveUpdateItem[] fullPUIs = this.fullPUIs;
		if (fullPUIs == null)
		{
			fullPUIs = new IPrimitiveUpdateItem[primitiveNameToIndexMap.size()];
			this.fullPUIs = fullPUIs;
		}
		int index = primitiveNameToIndexMap.get(pui.getMemberName()).intValue();
		if (fullPUIs[index] == null)
		{
			puiCount++;
		}
		fullPUIs[index] = pui;
	}

	public void addRelation(IRelationUpdateItem rui)
	{
		IRelationUpdateItem[] fullRUIs = this.fullRUIs;
		if (fullRUIs == null)
		{
			fullRUIs = new IRelationUpdateItem[relationNameToIndexMap.size()];
			this.fullRUIs = fullRUIs;
		}
		int index = relationNameToIndexMap.get(rui.getMemberName()).intValue();
		if (fullRUIs[index] == null)
		{
			ruiCount++;
		}
		fullRUIs[index] = rui;
	}

	public PrimitiveUpdateItem findPrimitive(String memberName)
	{
		if (fullPUIs == null)
		{
			return null;
		}
		return (PrimitiveUpdateItem) fullPUIs[primitiveNameToIndexMap.get(memberName)];
	}

	public RelationUpdateItemBuild findRelation(String memberName)
	{
		if (fullRUIs == null)
		{
			return null;
		}
		return (RelationUpdateItemBuild) fullRUIs[relationNameToIndexMap.get(memberName)];
	}

	public PrimitiveUpdateItem ensurePrimitive(String memberName)
	{
		PrimitiveUpdateItem pui = findPrimitive(memberName);
		if (pui != null)
		{
			return pui;
		}
		pui = new PrimitiveUpdateItem();
		pui.setMemberName(memberName);
		addPrimitive(pui);
		return pui;
	}

	public RelationUpdateItemBuild ensureRelation(String memberName)
	{
		RelationUpdateItemBuild rui = findRelation(memberName);
		if (rui != null)
		{
			return rui;
		}
		rui = new RelationUpdateItemBuild(memberName);
		addRelation(rui);
		return rui;
	}

	public int getPuiCount()
	{
		return puiCount;
	}

	public int getRuiCount()
	{
		return ruiCount;
	}
}
