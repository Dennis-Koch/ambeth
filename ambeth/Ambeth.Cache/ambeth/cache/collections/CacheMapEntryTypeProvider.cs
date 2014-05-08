using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Cache.Collections
{
    public class CacheMapEntryTypeProvider : ICacheMapEntryTypeProvider
    {
        public static readonly ICacheMapEntryFactory ci = new DefaultCacheMapEntryFactory();

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        [Autowired(Optional = true)]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        protected readonly Tuple2KeyHashMap<Type, sbyte, ICacheMapEntryFactory> typeToConstructorMap = new Tuple2KeyHashMap<Type, sbyte, ICacheMapEntryFactory>();

        protected readonly Lock writeLock = new ReadWriteLock().WriteLock;

        public ICacheMapEntryFactory GetCacheMapEntryType(Type entityType, sbyte idIndex)
        {
            if (BytecodeEnhancer == null)
            {
                return ci;
            }
            ICacheMapEntryFactory factory = typeToConstructorMap.Get(entityType, idIndex);
            if (factory != null)
            {
                return factory;
            }
            Lock writeLock = this.writeLock;
            writeLock.Lock();
            try
            {
                // concurrent thread might have been faster
                factory = typeToConstructorMap.Get(entityType, idIndex);
                if (factory != null)
                {
                    return factory;
                }
                try
			    {
                    Type enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(CacheMapEntry), new CacheMapEntryEnhancementHint(entityType, idIndex));
				    if (enhancedType == typeof(CacheMapEntry))
				    {
					    // Nothing has been enhanced
					    factory = ci;
				    }
				    else
				    {
					    factory = AccessorTypeProvider.GetConstructorType<ICacheMapEntryFactory>(enhancedType);
				    }
			    }
			    catch (Exception e)
			    {
				    if (Log.WarnEnabled)
				    {
					    Log.Warn(e);
				    }
				    // something serious happened during enhancement: continue with a fallback
				    factory = ci;
			    }
                typeToConstructorMap.Put(entityType, idIndex, factory);
                return factory;
            }
            finally
            {
                writeLock.Unlock();
            }
        }
    }
}