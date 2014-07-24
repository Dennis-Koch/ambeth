package de.osthus.ambeth.util;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

public class ValueHolderRef
{
	protected IObjRef objRef;

	protected IRelationInfoItem member;

	protected int relationIndex;

	public ValueHolderRef(IObjRef objRef, IRelationInfoItem member, int relationIndex)
	{
		this.objRef = objRef;
		this.member = member;
		this.relationIndex = relationIndex;
	}

	public IObjRef getObjRef()
	{
		return objRef;
	}

	public IRelationInfoItem getMember()
	{
		return member;
	}

	public int getRelationIndex()
	{
		return relationIndex;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof ValueHolderRef))
		{
			return false;
		}
		ValueHolderRef other = (ValueHolderRef) obj;
		return EqualsUtil.equals(getObjRef(), other.getObjRef()) && getRelationIndex() == other.getRelationIndex();
	}

	@Override
	public int hashCode()
	{
		return getObjRef().hashCode() ^ getRelationIndex();
	}
}
