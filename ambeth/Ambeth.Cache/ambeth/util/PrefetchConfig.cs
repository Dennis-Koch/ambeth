using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchConfig : IPrefetchConfig
    {
        protected readonly IDictionary<Type, IList<String>> typeToMembersToInitialize = new Dictionary<Type, IList<String>>();

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }
        
        public IPrefetchConfig Add(Type entityType, String propertyPath)
        {
            IList<String> membersToInitialize = DictionaryExtension.ValueOrDefault(typeToMembersToInitialize, entityType);
            if (membersToInitialize == null)
            {
                membersToInitialize = new List<String>();
                typeToMembersToInitialize.Add(entityType, membersToInitialize);
            }
            membersToInitialize.Add(propertyPath);
            return this;
        }

        public IPrefetchHandle Build()
        {
            return new PrefetchHandle(new Dictionary<Type, IList<String>>(typeToMembersToInitialize), PrefetchHelper);
        }
    }
}
