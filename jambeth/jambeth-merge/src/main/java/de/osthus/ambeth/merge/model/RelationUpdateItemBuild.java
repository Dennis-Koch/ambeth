package de.osthus.ambeth.merge.model;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.collections.EmptySet;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.merge.transfer.RelationUpdateItem;

public class RelationUpdateItemBuild implements IRelationUpdateItem
{
	protected String memberName;

	protected ISet<IObjRef> addedORIs = EmptySet.<IObjRef> emptySet();

	protected ISet<IObjRef> removedORIs = EmptySet.<IObjRef> emptySet();

	public RelationUpdateItemBuild(String memberName)
	{
		this.memberName = memberName;
	}

	@Override
	public String getMemberName()
	{
		return memberName;
	}

	@Override
	public IObjRef[] getAddedORIs()
	{
		if (addedORIs.size() == 0)
		{
			return null;
		}
		return addedORIs.toArray(IObjRef.class);
	}

	@Override
	public IObjRef[] getRemovedORIs()
	{
		if (removedORIs.size() == 0)
		{
			return null;
		}
		return removedORIs.toArray(IObjRef.class);
	}

	public void addObjRef(IObjRef objRef)
	{
		if (addedORIs.size() == 0)
		{
			addedORIs = new HashSet<IObjRef>();
		}
		addedORIs.add(objRef);
	}

	public void addObjRefs(IObjRef[] objRefs)
	{
		for (IObjRef objRef : objRefs)
		{
			addObjRef(objRef);
		}
	}

	public void addObjRefs(List<IObjRef> objRefs)
	{
		for (int a = 0, size = objRefs.size(); a < size; a++)
		{
			addObjRef(objRefs.get(a));
		}
	}

	public void addObjRefs(Collection<IObjRef> objRefs)
	{
		for (IObjRef objRef : objRefs)
		{
			addObjRef(objRef);
		}
	}

	public void removeObjRef(IObjRef objRef)
	{
		if (removedORIs.size() == 0)
		{
			removedORIs = new HashSet<IObjRef>();
		}
		removedORIs.add(objRef);
	}

	public void removeObjRefs(IObjRef[] objRefs)
	{
		for (IObjRef objRef : objRefs)
		{
			removeObjRef(objRef);
		}
	}

	public void removeObjRefs(List<IObjRef> objRefs)
	{
		for (int a = objRefs.size(); a-- > 0;)
		{
			removeObjRef(objRefs.get(a));
		}
	}

	public void removeObjRefs(Collection<IObjRef> objRefs)
	{
		for (IObjRef objRef : objRefs)
		{
			removeObjRef(objRef);
		}
	}

	public IRelationUpdateItem buildRUI()
	{
		RelationUpdateItem rui = new RelationUpdateItem();
		rui.setMemberName(memberName);
		rui.setAddedORIs(getAddedORIs());
		rui.setRemovedORIs(getRemovedORIs());
		return rui;
	}
}
