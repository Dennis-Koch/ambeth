package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.metadata.Member;

public interface IPrioMembersProvider
{
	IdentityLinkedSet<Member> getPrioMembers(ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPath, ArrayList<PrefetchCommand> pendingPrefetchCommands,
			MergePrefetchPathsCache mergePrefetchPathsCache);
}