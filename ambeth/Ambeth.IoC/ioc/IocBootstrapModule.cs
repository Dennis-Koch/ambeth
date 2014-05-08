using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Converter;
using System;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class IocBootstrapModule : IInitializingBootstrapModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }
                
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<ThreadPool>("threadPool").Autowireable(typeof(IThreadPool), typeof(IDelayedExecution));

            beanContextFactory.RegisterBean<DelegatingConversionHelper>("conversionHelper").PropertyRefs("*conversionHelper")
				.Autowireable(typeof(IConversionHelper), typeof(IDedicatedConverterExtendable));

		    beanContextFactory.RegisterBean<BooleanArrayConverter>("booleanArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "booleanArrayConverter", typeof(String), typeof(bool[]));

            beanContextFactory.RegisterBean<ByteArrayConverter>("byteArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "byteArrayConverter", typeof(String), typeof(byte[]));

            beanContextFactory.RegisterBean<SByteArrayConverter>("sbyteArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "sbyteArrayConverter", typeof(String), typeof(sbyte[]));
            
            beanContextFactory.RegisterBean<CharArrayConverter>("charArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "charArrayConverter", typeof(String), typeof(char[]));

            beanContextFactory.RegisterBean<InterningFeature>("interningFeature").Autowireable<IInterningFeature>();

            beanContextFactory.RegisterBean<GuiThreadHelper>("guiThreadHelper").Autowireable<IGuiThreadHelper>();
        }
    }
}
