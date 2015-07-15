using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Metadata;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
	public interface IPrioMembersProvider
	{
		IdentityLinkedSet<Member> GetPrioMembers(ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPath, List<PrefetchCommand> pendingPrefetchCommands,
				MergePrefetchPathsCache mergePrefetchPathsCache);
	}
}