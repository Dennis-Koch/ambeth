package de.osthus.ambeth.util;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.RelationMember;

public class ValueHolderRef
{
	protected IObjRef objRef;

	protected RelationMember member;

	protected int relationIndex;

	public ValueHolderRef(IObjRef objRef, RelationMember member, int relationIndex)
	{
		this.objRef = objRef;
		this.member = member;
		this.relationIndex = relationIndex;
	}

	public IObjRef getObjRef()
	{
		return objRef;
	}

	public RelationMember getMember()
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
