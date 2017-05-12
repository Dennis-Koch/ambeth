using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge.Incremental
{
    public class StateEntry
    {
        public readonly Object entity;

        public readonly IObjRef objRef;

        public readonly int index;

        public StateEntry(Object entity, IObjRef objRef, int index)
        {
            this.entity = entity;
            this.objRef = objRef;
            this.index = index;
        }
    }
}