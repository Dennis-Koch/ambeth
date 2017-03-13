package com.koch.ambeth.cache.util;

import java.lang.ref.WeakReference;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;

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