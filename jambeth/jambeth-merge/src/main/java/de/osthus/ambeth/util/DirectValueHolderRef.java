package de.osthus.ambeth.util;

import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.proxy.IObjRefContainer;

public class DirectValueHolderRef
{
	protected final IObjRefContainer vhc;

	protected final RelationMember member;

	protected final boolean objRefsOnly;

	public DirectValueHolderRef(IObjRefContainer vhc, RelationMember member)
	{
		this.vhc = vhc;
		this.member = member;
		this.objRefsOnly = false;
	}

	public DirectValueHolderRef(IObjRefContainer vhc, RelationMember member, boolean objRefsOnly)
	{
		this.vhc = vhc;
		this.member = member;
		this.objRefsOnly = objRefsOnly;
	}

	public IObjRefContainer getVhc()
	{
		return vhc;
	}

	public RelationMember getMember()
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
