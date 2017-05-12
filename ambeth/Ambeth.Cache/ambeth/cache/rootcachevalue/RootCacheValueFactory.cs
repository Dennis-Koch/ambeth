using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public class RootCacheValueFactory : IRootCacheValueFactory
    {
	    protected static readonly RootCacheValueFactoryDelegate rcvFactory = new DefaultRootCacheValueFactoryDelegate();

	    [LogInstance]
	    public ILogger Log { private get; set; }

        [Autowired(Optional = true)]
	    public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        [Autowired(Optional = true)]
	    public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired(Optional = true)]
        public IBytecodePrinter BytecodePrinter { protected get; set; }

        protected readonly HashMap<IEntityMetaData, RootCacheValueFactoryDelegate> typeToConstructorMap = new HashMap<IEntityMetaData, RootCacheValueFactoryDelegate>();

	    protected readonly Object writeLock = new Object();

	    public RootCacheValue CreateRootCacheValue(IEntityMetaData metaData)
	    {
            RootCacheValueFactoryDelegate rootCacheValueFactory = typeToConstructorMap.Get(metaData);
            if (rootCacheValueFactory != null)
            {
                return rootCacheValueFactory.CreateRootCacheValue(metaData);
            }
            if (BytecodeEnhancer == null)
            {
                return rcvFactory.CreateRootCacheValue(metaData);
            }
		    Object writeLock = this.writeLock;
		    lock (writeLock)
		    {
			    // concurrent thread might have been faster
                rootCacheValueFactory = typeToConstructorMap.Get(metaData);
                if (rootCacheValueFactory == null)
                {
                    rootCacheValueFactory = CreateDelegate(metaData);
                }
            }
            return rootCacheValueFactory.CreateRootCacheValue(metaData);
        }

        protected RootCacheValueFactoryDelegate CreateDelegate(IEntityMetaData metaData)
        {
            RootCacheValueFactoryDelegate rootCacheValueFactory;
            Type enhancedType = null;
            try
            {
                enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(RootCacheValue), new RootCacheValueEnhancementHint(metaData.EntityType));
                if (enhancedType == typeof(RootCacheValue))
                {
                    // Nothing has been enhanced
                    rootCacheValueFactory = rcvFactory;
                }
                else
                {
                    rootCacheValueFactory = AccessorTypeProvider.GetConstructorType<RootCacheValueFactoryDelegate>(enhancedType);
                }
            }
            catch (Exception e)
            {
                if (Log.WarnEnabled)
                {
                    Log.Warn(BytecodePrinter.ToPrintableBytecode(enhancedType), e);
                }
                // something serious happened during enhancement: continue with a fallback
                rootCacheValueFactory = rcvFactory;
            }
            typeToConstructorMap.Put(metaData, rootCacheValueFactory);
            return rootCacheValueFactory;
        }
    }
}