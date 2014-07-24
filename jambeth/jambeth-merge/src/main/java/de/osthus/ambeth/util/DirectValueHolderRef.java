package de.osthus.ambeth.util;

import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

public class DirectValueHolderRef
{
	protected final IObjRefContainer vhc;

	protected final IRelationInfoItem member;

	protected final boolean objRefsOnly;

	public DirectValueHolderRef(IObjRefContainer vhc, IRelationInfoItem member)
	{
		this.vhc = vhc;
		this.member = member;
		this.objRefsOnly = false;
	}

	public DirectValueHolderRef(IObjRefContainer vhc, IRelationInfoItem member, boolean objRefsOnly)
	{
		this.vhc = vhc;
		this.member = member;
		this.objRefsOnly = objRefsOnly;
	}

	public IObjRefContainer getVhc()
	{
		return vhc;
	}

	public IRelationInfoItem getMember()
	{
		return member;
	}

	public boolean isObjRefsOnly()
	{
		return objRefsOnly;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof DirectValueHolderRef))
		{
			return false;
		}
		DirectValueHolderRef other = (DirectValueHolderRef) obj;
		return vhc == other.vhc && member == other.member;
	}

	@Override
	public int hashCode()
	{
		return vhc.hashCode() ^ member.hashCode();
	}
}
