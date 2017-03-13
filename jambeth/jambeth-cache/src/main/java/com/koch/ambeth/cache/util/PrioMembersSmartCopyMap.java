package com.koch.ambeth.cache.util;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.IMapEntry;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class PrioMembersSmartCopyMap extends SmartCopyMap<PrioMembersKey, IdentityLinkedSet<Member>>
{
	public PrioMembersSmartCopyMap()
	{
		super(0.5f);
	}

	@Override
	protected boolean equalKeys(PrioMembersKey key, IMapEntry<PrioMembersKey, IdentityLinkedSet<Member>> entry)
	{
		PrioMembersKey other = entry.getKey();
		if (key == other)
		{
			return true;
		}
		IdentityLinkedSet<Member> key1 = key.getKey1();
		IdentityLinkedSet<Member> otherKey1 = other.getKey1();
		if (key1.size() != otherKey1.size())
		{
			return false;
		}
		for (Member item : key1)
		{
			if (!otherKey1.contains(item))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	protected int extractHash(PrioMembersKey key)
	{
		IdentityLinkedSet<Member> key1 = key.getKey1();
		int hash = 91 ^ key1.size();
		for (Member item : key1)
		{
			hash ^= item.hashCode();
		}
		return hash;
	}
}