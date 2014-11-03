﻿using De.Osthus.Ambeth.Accessor;
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

            IBeanConfiguration stringToFileConverter = beanContextFactory.RegisterAnonymousBean<StringToFileConverter>();
		    DedicatedConverterUtil.BiLink(beanContextFactory, stringToFileConverter, typeof(String), typeof(FileInfo));
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToFileConverter, typeof(String), typeof(DirectoryInfo));

            IBeanConfiguration stringToClassArrayConverter = beanContextFactory.RegisterAnonymousBean<StringToClassArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToClassArrayConverter, typeof(String), typeof(Type[]));

            IBeanConfiguration stringToDoubleArrayConverter = beanContextFactory.RegisterAnonymousBean<StringToDoubleArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToDoubleArrayConverter, typeof(String), typeof(double[]));

            IBeanConfiguration stringToFloatArrayConverter = beanContextFactory.RegisterAnonymousBean<StringToFloatArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToFloatArrayConverter, typeof(String), typeof(float[]));

            IBeanConfiguration stringToIntArrayConverter = beanContextFactory.RegisterAnonymousBean<StringToIntArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToIntArrayConverter, typeof(String), typeof(int[]));

            IBeanConfiguration stringToLongArrayConverter = beanContextFactory.RegisterAnonymousBean<StringToLongArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToLongArrayConverter, typeof(String), typeof(long[]));

            IBeanConfiguration stringToPatternConverterBC = beanContextFactory.RegisterAnonymousBean<StringToPatternConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToPatternConverterBC, typeof(String), typeof(Regex));
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToPatternConverterBC, typeof(String), typeof(Regex[]));

            IBeanConfiguration stringToStringArrayConverter = beanContextFactory.RegisterAnonymousBean<StringToStringArrayConverter>();
            DedicatedConverterUtil.BiLink(beanContextFactory, stringToStringArrayConverter, typeof(String), typeof(String[]));

            beanContextFactory.RegisterBean<InterningFeature>("interningFeature").Autowireable<IInterningFeature>();

            beanContextFactory.RegisterBean<GuiThreadHelper>("guiThreadHelper").Autowireable<IGuiThreadHelper>();
        }
    }
}
