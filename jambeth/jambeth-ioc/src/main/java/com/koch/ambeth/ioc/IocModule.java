package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.cancel.Cancellation;
import com.koch.ambeth.ioc.cancel.ICancellation;
import com.koch.ambeth.ioc.cancel.ICancellationWritable;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.converter.FileToPathConverter;
import com.koch.ambeth.ioc.converter.StringToBlobConverter;
import com.koch.ambeth.ioc.converter.StringToCharsetConverter;
import com.koch.ambeth.ioc.converter.StringToClassArrayConverter;
import com.koch.ambeth.ioc.converter.StringToDoubleArrayConverter;
import com.koch.ambeth.ioc.converter.StringToFileConverter;
import com.koch.ambeth.ioc.converter.StringToFloatArrayConverter;
import com.koch.ambeth.ioc.converter.StringToIntArrayConverter;
import com.koch.ambeth.ioc.converter.StringToLongArrayConverter;
import com.koch.ambeth.ioc.converter.StringToPathConverter;
import com.koch.ambeth.ioc.converter.StringToPatternConverter;
import com.koch.ambeth.ioc.converter.StringToStringArrayConverter;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.jaxb.IJAXBContextProvider;
import com.koch.ambeth.ioc.jaxb.JAXBContextProvider;
import com.koch.ambeth.ioc.proxy.CgLibUtil;
import com.koch.ambeth.ioc.proxy.ICgLibUtil;
import com.koch.ambeth.ioc.util.DedicatedConverterUtil;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.ioc.util.MultithreadingHelper;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.converter.BooleanArrayConverter;
import com.koch.ambeth.util.converter.ByteArrayConverter;
import com.koch.ambeth.util.converter.CharArrayConverter;
import com.koch.ambeth.util.factory.EmptyArrayFactory;
import com.koch.ambeth.util.factory.IEmptyArrayFactory;
import com.koch.ambeth.util.objectcollector.AppendableStringBuilderCollectableController;
import com.koch.ambeth.util.objectcollector.ICollectableControllerExtendable;
import com.koch.ambeth.util.threading.FastThreadPool;
import com.koch.ambeth.util.threading.GuiThreadHelper;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import io.toolisticon.spiap.api.SpiService;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Blob;
import java.util.regex.Pattern;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class IocModule implements IFrameworkModule {
    public static final String THREAD_POOL_NAME = "threadPool";

    @Property(name = IocConfigurationConstants.JavaUiActive, defaultValue = "true")
    protected boolean javaUiActive;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean("booleanArrayConverter", BooleanArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, "booleanArrayConverter", String.class, boolean[].class);

        beanContextFactory.registerBean("byteArrayConverter", ByteArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, "byteArrayConverter", String.class, byte[].class);

        beanContextFactory.registerBean("charArrayConverter", CharArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, "charArrayConverter", String.class, char[].class);

        var fileToPathConverter = beanContextFactory.registerBean(FileToPathConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, fileToPathConverter, File.class, Path.class);
        DedicatedConverterUtil.biLink(beanContextFactory, fileToPathConverter, File[].class, Path[].class);

        var stringToPathConverter = beanContextFactory.registerBean(StringToPathConverter.class);
        DedicatedConverterUtil.link(beanContextFactory, stringToPathConverter, String.class, Path.class);
        DedicatedConverterUtil.link(beanContextFactory, stringToPathConverter, String.class, Path[].class);

        beanContextFactory.registerBean(Cancellation.class).autowireable(ICancellation.class, ICancellationWritable.class);

        var stringToBlobConverter = beanContextFactory.registerBean(StringToBlobConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToBlobConverter, String.class, Blob.class);

        var stringToFileConverter = beanContextFactory.registerBean(StringToFileConverter.class);
        DedicatedConverterUtil.link(beanContextFactory, stringToFileConverter, String.class, File.class);
        DedicatedConverterUtil.link(beanContextFactory, stringToFileConverter, String.class, File[].class);

        var stringToCharsetConverter = beanContextFactory.registerBean(StringToCharsetConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToCharsetConverter, String.class, Charset.class);

        var stringToClassArrayConverter = beanContextFactory.registerBean(StringToClassArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToClassArrayConverter, String.class, Class[].class);

        var stringToDoubleArrayConverter = beanContextFactory.registerBean(StringToDoubleArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToDoubleArrayConverter, String.class, double[].class);

        var stringToFloatArrayConverter = beanContextFactory.registerBean(StringToFloatArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToFloatArrayConverter, String.class, float[].class);

        var stringToIntArrayConverter = beanContextFactory.registerBean(StringToIntArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToIntArrayConverter, String.class, int[].class);

        var stringToLongArrayConverter = beanContextFactory.registerBean(StringToLongArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToLongArrayConverter, String.class, long[].class);

        var stringToPatternConverterBC = beanContextFactory.registerBean(StringToPatternConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToPatternConverterBC, String.class, Pattern.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToPatternConverterBC, String.class, Pattern[].class);

        var stringToStringArrayConverter = beanContextFactory.registerBean(StringToStringArrayConverter.class);
        DedicatedConverterUtil.biLink(beanContextFactory, stringToStringArrayConverter, String.class, String[].class);

        var appendableStringBuilderCollectableController = beanContextFactory.registerBean(AppendableStringBuilderCollectableController.class);
        beanContextFactory.link(appendableStringBuilderCollectableController).to(ICollectableControllerExtendable.class).with(AppendableStringBuilder.class);

        beanContextFactory.registerBean("cgLibUtil", CgLibUtil.class).autowireable(ICgLibUtil.class);

        beanContextFactory.registerBean("guiThreadHelper", GuiThreadHelper.class)
                          .propertyRef("Executor", THREAD_POOL_NAME)
                          .propertyValue("JavaUiActive", javaUiActive)
                          .autowireable(IGuiThreadHelper.class);

        beanContextFactory.registerBean(JAXBContextProvider.class).autowireable(IJAXBContextProvider.class);

        var fastThreadPool = new FastThreadPool(0, Integer.MAX_VALUE, 60000) {
            @Override
            public void refreshThreadCount() {
                if (variableThreads) {
                    int processors = Runtime.getRuntime().availableProcessors();
                    setMaxThreadCount(processors * 2);
                }
            }
        };
        fastThreadPool.setName("MTH");
        fastThreadPool.refreshThreadCount();

        var fastThreadPoolBean = beanContextFactory.registerExternalBean(THREAD_POOL_NAME, fastThreadPool);

        beanContextFactory.registerDisposable(fastThreadPool);

        beanContextFactory.registerBean(MultithreadingHelper.class).autowireable(IMultithreadingHelper.class).propertyRef(fastThreadPoolBean);

        beanContextFactory.registerBean(EmptyArrayFactory.class).autowireable(IEmptyArrayFactory.class);
    }
}
