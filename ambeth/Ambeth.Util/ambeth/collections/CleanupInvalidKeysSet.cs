using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Collections
{
    public class CleanupInvalidKeysSet<K> : CHashSet<K>
    {
        protected readonly IInvalidKeyChecker<K> invalidKeyChecker;

	    protected int modCountDownCount;

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker)
            : base()
	    {
		    this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
	    }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, ICollection<K> sourceCollection)
            : base(sourceCollection)
	    {
		    this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
	    }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, float loadFactor)
            : base(loadFactor)
	    {
		    this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
	    }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, IList sourceCollection)
            : base(sourceCollection)
        {
            this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
        }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, IList<K> sourceCollection)
            : base(sourceCollection)
        {
            this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
        }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, K[] sourceArray)
            : base(sourceArray)
        {
            this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
        }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, int initialCapacity)
            : base(initialCapacity)
        {
            this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
        }

        public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, int initialCapacity, float loadFactor)
            : base(initialCapacity, loadFactor)
        {
            this.invalidKeyChecker = invalidKeyChecker;
            ResetCounter();
        }

        protected override void EntryAdded(SetEntry<K> entry)
        {
 	         base.EntryAdded(entry);
		     SetChanged();
	    }

        protected override void EntryRemoved(SetEntry<K> entry)
        {
 	         base.EntryRemoved(entry);
             SetChanged();
	    }

        protected void ResetCounter()
        {
            modCountDownCount = threshold / 2 + 1;
        }

	    protected void SetChanged()
	    {
		    if (--modCountDownCount != 0)
		    {
			    return;
		    }
		    // size() can never be 0 after super.entryAdded(). So this statement is only true if we want the "one-time" cleanup operation
		    // between two transfer() calls
            modCountDownCount = 0; // set the counter to zero to suppress triggering of "setChanged()" from entryRemoved()
            Transfer(table);
            ResetCounter();
	    }

        protected override void Resize(int newCapacity)
        {
            modCountDownCount = 0; // set the counter to zero to suppress triggering of "setChanged()" from entryRemoved()
            base.Resize(newCapacity);
            ResetCounter();
        }

        protected override bool IsEntryValid(De.Osthus.Ambeth.Collections.SetEntry<K> entry)
        {
            return invalidKeyChecker.IsKeyValid(entry.Key);
	    }
    }
}