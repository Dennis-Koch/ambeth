using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using System.Collections;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class PrefetchFuture : IObjectFuture
    {
        public IEnumerable ToPrefetch { get; private set; }

        public PrefetchFuture(IEnumerable toPrefetch)
        {
            ToPrefetch = toPrefetch;
        }

        public object Value
        {
            get { throw new NotImplementedException(); }
        }
    }
}
