using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System.Collections;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class PrefetchFutureHandler : IObjectFutureHandler, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual IPrefetchHelper PrefetchHelper { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(PrefetchHelper, "PrefetchHelper");
        }

        public void Handle(IList<IObjectFuture> objectFutures)
        {
            List<IEnumerable> allToPrefetch = new List<IEnumerable>(objectFutures.Count);
            for (int i = 0, size = objectFutures.Count; i < size; i++)
            {
                IObjectFuture objectFuture = objectFutures[i];
                if (!(objectFuture is PrefetchFuture))
                {
                    throw new ArgumentException("'" + GetType().Name + "' cannot handle " + typeof(IObjectFuture).Name
                            + " implementations of type '" + objectFuture.GetType().Name + "'");
                }

                PrefetchFuture prefetchFuture = (PrefetchFuture)objectFuture;
                IEnumerable toPrefetch = prefetchFuture.ToPrefetch;
                allToPrefetch.Add(toPrefetch);
            }

            PrefetchHelper.Prefetch(allToPrefetch);
        }
    }
}
