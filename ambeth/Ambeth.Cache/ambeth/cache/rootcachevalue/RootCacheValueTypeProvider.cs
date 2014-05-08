using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public class RootCacheValueTypeProvider : IRootCacheValueTypeProvider
    {
	    protected static readonly ConstructorInfo ci;

	    static RootCacheValueTypeProvider()
	    {
    	    ci = typeof(DefaultRootCacheValue).GetConstructor(new Type[] { typeof(Type) });
	    }

	    [LogInstance]
	    public ILogger Log { private get; set; }

        [Autowired(Optional = true)]
	    public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

	    protected readonly HashMap<Type, ConstructorInfo> typeToConstructorMap = new HashMap<Type, ConstructorInfo>();

	    protected readonly Object writeLock = new Object();

	    public ConstructorInfo GetRootCacheValueType(Type entityType)
	    {
		    if (BytecodeEnhancer == null)
		    {
			    return ci;
		    }
		    ConstructorInfo constructor = typeToConstructorMap.Get(entityType);
		    if (constructor != null)
		    {
			    return constructor;
		    }
		    Object writeLock = this.writeLock;
		    lock (writeLock)
		    {
			    // concurrent thread might have been faster
			    constructor = typeToConstructorMap.Get(entityType);
			    if (constructor != null)
			    {
				    return constructor;
			    }
                Type enhancedType;
                try
                {
                    enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(RootCacheValue), new RootCacheValueEnhancementHint(entityType));
                }
                catch (Exception e)
                {
                    if (Log.WarnEnabled)
                    {
                        Log.Warn(e);
                    }
                    // something serious happened during enhancement: continue with a fallback
                    enhancedType = typeof(RootCacheValue);
                }
				if (enhancedType == typeof(RootCacheValue))
				{
					// Nothing has been enhanced
					constructor = ci;
				}
				else
				{
                    constructor = enhancedType.GetConstructor(new Type[] { typeof(Type) });
				}
				typeToConstructorMap.Put(entityType, constructor);
			    return constructor;
		    }
	    }
    }
}