package com.koch.ambeth.merge.bytecode.compositeid;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.IPrintable;

public class CompositeIdEnhancementHint implements IEnhancementHint, IPrintable, ITargetNameEnhancementHint
{
	private final Member[] idMembers;

	public CompositeIdEnhancementHint(Member[] idMembers)
	{
		this.idMembers = idMembers;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getPackage().getName()).append('.').append("CompositeId");
		for (int a = 0, size = idMembers.length; a < size; a++)
		{
			Member idMember = idMembers[a];
			sb.append('$').append(idMember.getName());
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof CompositeIdEnhancementHint))
		{
			return false;
		}
		CompositeIdEnhancementHint other = (CompositeIdEnhancementHint) obj;
		if (other.idMembers.length != idMembers.length)
		{
			return false;
		}
		for (int a = idMembers.length; a-- > 0;)
		{
			Member idMember = idMembers[a];
			Member otherIdMember = other.idMembers[a];
			if (!EqualsUtil.equals(idMember.getName(), otherIdMember.getName()) || !EqualsUtil.equals(idMember.getRealType(), otherIdMember.getRealType()))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		int hash = CompositeIdEnhancementHint.class.hashCode();
		for (int a = idMembers.length; a-- > 0;)
		{
			Member idMember = idMembers[a];
			hash ^= idMember.getName().hashCode() ^ idMember.getRealType().hashCode();
		}
		return hash;
	}

	public Member[] getIdMembers()
	{
		return idMembers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (CompositeIdEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getName()).append(": ");
		for (int a = 0, size = idMembers.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append(',');
			}
			sb.append(idMembers[a].getName());
		}
	}
}
