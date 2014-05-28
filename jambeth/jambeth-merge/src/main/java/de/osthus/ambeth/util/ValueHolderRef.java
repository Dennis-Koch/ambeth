package de.osthus.ambeth.util;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

public class ValueHolderRef
{
	protected IObjRef objRef;

	protected IRelationInfoItem member;

	public ValueHolderRef(IObjRef objRef, IRelationInfoItem member)
	{
		this.objRef = objRef;
		this.member = member;
	}

	public IObjRef getObjRef()
	{
		return objRef;
	}

	public IRelationInfoItem getMember()
	{
		return member;
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
		return EqualsUtil.equals(getObjRef(), other.getObjRef()) && EqualsUtil.equals(getMember(), other.getMember());
	}

	@Override
	public int hashCode()
	{
		return getObjRef().hashCode() ^ getMember().hashCode();
	}
}
