using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Collections
{
    /**
     * This special kind of HashMap is intended to be used in high-performance concurrent scenarios with many reads and only some single occurences of write
     * accesses. To allow extremely high concurrency there is NO lock in read access scenarios. The design pattern to synchronize the reads with the indeed
     * synchronized write accesses the internal table-structure well be REPLACED on each write.
     * 
     * Because of this the existing internal object graph will NEVER be modified allowing unsynchronized read access of any amount without performance loss.
     * 
     * @param <K>
     * @param <V>
     */
    public class IdentityWeakSmartCopyMap<K, V> : WeakSmartCopyMap<K, V> where V : class
    {
	    public IdentityWeakSmartCopyMap() : base()
	    {
		    // intended blank
	    }

	    public IdentityWeakSmartCopyMap(float loadFactor) : base(loadFactor)
	    {
		    // intended blank
	    }

	    public IdentityWeakSmartCopyMap(int initialCapacity, float loadFactor) : base(initialCapacity, loadFactor)
	    {
            // intended blank		    
	    }

	    public IdentityWeakSmartCopyMap(int initialCapacity) : base(initialCapacity, 0.5f)
	    {
            // intended blank		    
	    }

        protected override int ExtractHash(K key)
        {
            return RuntimeHelpers.GetHashCode(key);
        }

        protected override bool EqualKeys(K key, IMapEntry<K, V> entry)
        {
            return Object.ReferenceEquals(key, entry.Key);
        }
    }
}