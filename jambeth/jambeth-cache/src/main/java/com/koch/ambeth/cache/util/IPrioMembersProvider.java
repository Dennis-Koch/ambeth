package com.koch.ambeth.cache.util;

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;

public interface IPrioMembersProvider
{
	IdentityLinkedSet<Member> getPrioMembers(ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPath, ArrayList<PrefetchCommand> pendingPrefetchCommands,
			MergePrefetchPathsCache mergePrefetchPathsCache);
}