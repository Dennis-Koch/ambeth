using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Util
{
	public class PrioMembersSmartCopyMap : SmartCopyMap<PrioMembersKey, IdentityLinkedSet<Member>>
	{
		public PrioMembersSmartCopyMap() : base(0.5f)
		{
			// Intended blank
		}

		protected override bool EqualKeys(PrioMembersKey key, MapEntry<PrioMembersKey, IdentityLinkedSet<Member>> entry)
		{
			PrioMembersKey other = entry.Key;
			if (key == other)
			{
				return true;
			}
			IdentityLinkedSet<Member> key1 = key.Key1;
			IdentityLinkedSet<Member> otherKey1 = other.Key1;
			if (key1.Count != otherKey1.Count)
			{
				return false;
			}
			foreach (Member item in key1)
			{
				if (!otherKey1.Contains(item))
				{
					return false;
				}
			}
			return true;
		}

		protected override int ExtractHash(PrioMembersKey key)
		{
			IdentityLinkedSet<Member> key1 = key.Key1;
			int hash = 91 ^ key1.Count;
			foreach (Member item in key1)
			{
				hash ^= item.GetHashCode();
			}
			return hash;
		}
	}
}