using De.Osthus.Ambeth.Converter;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Converter;
using System;
using System.IO;
using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class IocModule : IInitializingModule
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<ThreadPool>("threadPool").Autowireable(typeof(IThreadPool), typeof(IDelayedExecution));

            beanContextFactory.RegisterBean<BooleanArrayConverter>("booleanArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "booleanArrayConverter", typeof(String), typeof(bool[]));

            beanContextFactory.RegisterBean<ByteArrayConverter>("byteArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "byteArrayConverter", typeof(String), typeof(byte[]));

            beanContextFactory.RegisterBean<SByteArrayConverter>("sbyteArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "sbyteArrayConverter", typeof(String), typeof(sbyte[]));

            beanContextFactory.RegisterBean<CharArrayConverter>("charArrayConverter");
            DedicatedConverterUtil.BiLink(beanContextFactory, "charArrayConverter", typeof(String), typeof(char[]));

            IBeanConfiguration stringToFileConverter = beanContextFactory.RegisterBean<StringToFileConverter>();
		    DedicatedConverterUtil.Link(beanContextFactory, stringToFileConverter, typeof(String), typeof(FileInfo));
            DedicatedConverterUtil.Link(beanContextFactory, stringToFileConverter, typeof(String), typeof(DirectoryInfo));

            IBeanConfiguration stringToClassArrayConverter = beanContextFactory.RegisterBean<StringToClassArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToClassArrayConverter, typeof(String), typeof(Type[]));

            IBeanConfiguration stringToDoubleArrayConverter = beanContextFactory.RegisterBean<StringToDoubleArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToDoubleArrayConverter, typeof(String), typeof(double[]));

            IBeanConfiguration stringToFloatArrayConverter = beanContextFactory.RegisterBean<StringToFloatArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToFloatArrayConverter, typeof(String), typeof(float[]));

            IBeanConfiguration stringToIntArrayConverter = beanContextFactory.RegisterBean<StringToIntArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToIntArrayConverter, typeof(String), typeof(int[]));

            IBeanConfiguration stringToLongArrayConverter = beanContextFactory.RegisterBean<StringToLongArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToLongArrayConverter, typeof(String), typeof(long[]));

            IBeanConfiguration stringToPatternConverterBC = beanContextFactory.RegisterBean<StringToPatternConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToPatternConverterBC, typeof(String), typeof(Regex));
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToPatternConverterBC, typeof(String), typeof(Regex[]));

            IBeanConfiguration stringToStringArrayConverter = beanContextFactory.RegisterBean<StringToStringArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToStringArrayConverter, typeof(String), typeof(String[]));

            beanContextFactory.RegisterBean<InterningFeature>("interningFeature").Autowireable<IInterningFeature>();

            beanContextFactory.RegisterBean<GuiThreadHelper>("guiThreadHelper").Autowireable<IGuiThreadHelper>();
            
            beanContextFactory.RegisterBean<MultithreadingHelper>().Autowireable<IMultithreadingHelper>();
        }
    }
}
