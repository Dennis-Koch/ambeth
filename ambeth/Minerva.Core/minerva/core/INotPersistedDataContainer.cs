using System.Collections.Generic;
using System.ComponentModel;
using System.Collections;
using System;

namespace De.Osthus.Minerva.Core
{
    // Interface for tracking not persisted data.
    // GetNotPersistedData returns a copy with all not persisted objects, that can be
    // used for Save or RevertChanges. The HasNotPersisted method can be used for a quick
    // check whether GetNotPersistedData would return a list with at least one item.
    // NotPersistedChanged event is fired whenever the number of not persisted data changes:
    public interface INotPersistedDataContainer
    {
        event PropertyChangedEventHandler NotPersistedChanged;

        bool HasNotPersisted();

        IList<Object> GetNotPersistedDataRaw();
    }

    public interface INotPersistedDataContainer<T> : INotPersistedDataContainer
    {
        IList<T> GetNotPersistedData();
    }
}
