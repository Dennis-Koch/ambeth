using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchHandle : IPrefetchHandle
    {
        protected IDictionary<Type, IList<String>> typeToMembersToInitialize;

        protected IPrefetchHelper prefetchHelper;

        public PrefetchHandle(IDictionary<Type, IList<String>> typeToMembersToInitialize, IPrefetchHelper prefetchHelper)
        {
            this.typeToMembersToInitialize = typeToMembersToInitialize;
            this.prefetchHelper = prefetchHelper;
        }

        public IPrefetchState Prefetch(Object objects)
        {
            return prefetchHelper.Prefetch(objects, typeToMembersToInitialize);
        }

        public IPrefetchState Prefetch(params Object[] objects)
        {
            return prefetchHelper.Prefetch(objects, typeToMembersToInitialize);
        }

        public IPrefetchHandle Union(IPrefetchHandle otherPrefetchHandle)
        {
            if (otherPrefetchHandle == null)
            {
                return this;
            }
            Dictionary<Type, IList<String>> unionMap = new Dictionary<Type,IList<String>>();
            foreach (KeyValuePair<Type, IList<String>> entry in typeToMembersToInitialize)
            {
                unionMap.Add(entry.Key, new List<String>(entry.Value));
            }
            foreach (KeyValuePair<Type, IList<String>> entry in ((PrefetchHandle)otherPrefetchHandle).typeToMembersToInitialize)
            {
                IList<String> prefetchPaths = DictionaryExtension.ValueOrDefault(unionMap, entry.Key);
                if (prefetchPaths == null)
                {
                    prefetchPaths = new List<String>();
                    unionMap.Add(entry.Key, prefetchPaths);
                }
                foreach (String prefetchPath in entry.Value)
                {
                    if (prefetchPaths.Contains(prefetchPath))
                    {
                        continue;
                    }
                    prefetchPaths.Add(prefetchPath);
                }
            }
            return new PrefetchHandle(unionMap, prefetchHelper);
        }
    }
}