package de.osthus.ambeth.util;

import java.lang.ref.WeakReference;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.metadata.Member;

public class PrioMembersKey extends WeakReference<ILinkedMap<Class<?>, PrefetchPath[]>>
{
	private final IdentityLinkedSet<Member> key1;

	public PrioMembersKey(ILinkedMap<Class<?>, PrefetchPath[]> referent, IdentityLinkedSet<Member> key1)
	{
		super(referent);
		this.key1 = key1;
	}

	public IdentityLinkedSet<Member> getKey1()
	{
		return key1;
	}
}