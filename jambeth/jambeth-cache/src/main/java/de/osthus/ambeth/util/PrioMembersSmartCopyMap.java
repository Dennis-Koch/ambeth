package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.IMapEntry;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.metadata.Member;

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