using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Metadata;
using System;

namespace De.Osthus.Ambeth.Util
{
	public class PrioMembersKey : WeakReference
	{
		private readonly IdentityLinkedSet<Member> key1;

		public PrioMembersKey(ILinkedMap<Type, PrefetchPath[]> referent, IdentityLinkedSet<Member> key1) : base (referent)
		{
			this.key1 = key1;
		}

		public IdentityLinkedSet<Member> Key1
		{
			get
			{
				return key1;
			}
		}
	}
}