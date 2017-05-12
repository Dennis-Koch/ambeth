using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text.RegularExpressions;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Service
{
    public class CacheServiceModelProvider
    {
        public static void Init()
        {
            FullServiceModelProvider.ShareServiceModel(Assembly.GetExecutingAssembly(),
                "De\\.Osthus\\.Ambeth.*\\.Transfer.*");
            AssemblyHelper.RegisterExecutingAssembly();
        }

        static public IEnumerable<Type> RegisterKnownTypes(ICustomAttributeProvider provider)
        {
            return FullServiceModelProvider.RegisterKnownTypes(provider);
        }  
    }
}
