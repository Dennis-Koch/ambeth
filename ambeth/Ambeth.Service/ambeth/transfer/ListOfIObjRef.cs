using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [CollectionDataContract]
    public class ListOfIObjRef : List<IObjRef>
    {
        public ListOfIObjRef() : base()
        {
            // Intended blank
        }

        public ListOfIObjRef(IEnumerable<IObjRef> collection) : base(collection)
        {
            // Intended blank
        }

        public ListOfIObjRef(int capacity)
            : base(capacity)
        {
            // Intended blank
        }
    }
}
