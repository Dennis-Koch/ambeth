package com.koch.ambeth.ioc;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Blob;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.cancel.Cancellation;
import com.koch.ambeth.ioc.cancel.ICancellation;
import com.koch.ambeth.ioc.cancel.ICancellationWritable;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.InterningFeature;
import com.koch.ambeth.util.JREVersionProvider;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.converter.BooleanArrayConverter;
import com.koch.ambeth.util.converter.ByteArrayConverter;
import com.koch.ambeth.util.converter.CharArrayConverter;
import com.koch.ambeth.util.objectcollector.AppendableStringBuilderCollectableController;
import com.koch.ambeth.util.objectcollector.ICollectableControllerExtendable;
import com.koch.ambeth.util.threading.FastThreadPool;
import com.koch.ambeth.util.threading.GuiThreadHelper;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

@FrameworkModule
public class IocModule implements IInitializingModule
{
	public static final String THREAD_POOL_NAME = "threadPool";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = IocConfigurationConstants.JavaUiActive, defaultValue = "true")
	protected boolean javaUiActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("booleanArrayConverter", BooleanArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "booleanArrayConverter", String.class, boolean[].class);

		beanContextFactory.registerBean("byteArrayConverter", ByteArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "byteArrayConverter", String.class, byte[].class);

		beanContextFactory.registerBean("charArrayConverter", CharArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "charArrayConverter", String.class, char[].class);

		if (JREVersionProvider.getVersion() >= 1.7)
		{
			IBeanConfiguration fileToPathConverter = beanContextFactory.registerBean(FileToPathConverter.class);
			DedicatedConverterUtil.biLink(beanContextFactory, fileToPathConverter, File.class, Path.class);
			DedicatedConverterUtil.biLink(beanContextFactory, fileToPathConverter, File[].class, Path[].class);

			IBeanConfiguration stringToPathConverter = beanContextFactory.registerBean(StringToPathConverter.class);
			DedicatedConverterUtil.link(beanContextFactory, stringToPathConverter, String.class, Path.class);
			DedicatedConverterUtil.link(beanContextFactory, stringToPathConverter, String.class, Path[].class);
		}

		beanContextFactory.registerBean(Cancellation.class).autowireable(ICancellation.class, ICancellationWritable.class);

		IBeanConfiguration stringToBlobConverter = beanContextFactory.registerBean(StringToBlobConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToBlobConverter, String.class, Blob.class);

		IBeanConfiguration stringToFileConverter = beanContextFactory.registerBean(StringToFileConverter.class);
		DedicatedConverterUtil.link(beanContextFactory, stringToFileConverter, String.class, File.class);
		DedicatedConverterUtil.link(beanContextFactory, stringToFileConverter, String.class, File[].class);

		IBeanConfiguration stringToCharsetConverter = beanContextFactory.registerBean(StringToCharsetConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToCharsetConverter, String.class, Charset.class);

		IBeanConfiguration stringToClassArrayConverter = beanContextFactory.registerBean(StringToClassArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToClassArrayConverter, String.class, Class[].class);

		IBeanConfiguration stringToDoubleArrayConverter = beanContextFactory.registerBean(StringToDoubleArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToDoubleArrayConverter, String.class, double[].class);

		IBeanConfiguration stringToFloatArrayConverter = beanContextFactory.registerBean(StringToFloatArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToFloatArrayConverter, String.class, float[].class);

		IBeanConfiguration stringToIntArrayConverter = beanContextFactory.registerBean(StringToIntArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToIntArrayConverter, String.class, int[].class);

		IBeanConfiguration stringToLongArrayConverter = beanContextFactory.registerBean(StringToLongArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToLongArrayConverter, String.class, long[].class);

		IBeanConfiguration stringToPatternConverterBC = beanContextFactory.registerBean(StringToPatternConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToPatternConverterBC, String.class, Pattern.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToPatternConverterBC, String.class, Pattern[].class);

		IBeanConfiguration stringToStringArrayConverter = beanContextFactory.registerBean(StringToStringArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToStringArrayConverter, String.class, String[].class);

		IBeanConfiguration appendableStringBuilderCollectableController = beanContextFactory.registerBean(AppendableStringBuilderCollectableController.class);
		beanContextFactory.link(appendableStringBuilderCollectableController).to(ICollectableControllerExtendable.class).with(AppendableStringBuilder.class);

		beanContextFactory.registerBean("interningFeature", InterningFeature.class).autowireable(IInterningFeature.class);

		beanContextFactory.registerBean("cgLibUtil", CgLibUtil.class).autowireable(ICgLibUtil.class);

		beanContextFactory.registerBean("guiThreadHelper", GuiThreadHelper.class).propertyRef("Executor", THREAD_POOL_NAME)
				.propertyValue("javaUiActive", javaUiActive).autowireable(IGuiThreadHelper.class);

		beanContextFactory.registerBean(JAXBContextProvider.class).autowireable(IJAXBContextProvider.class);

		final FastThreadPool fastThreadPool = new FastThreadPool(0, Integer.MAX_VALUE, 60000)
		{
			@Override
			public void refreshThreadCount()
			{
				if (variableThreads)
				{
					int processors = Runtime.getRuntime().availableProcessors();
					setMaxThreadCount(processors * 2);
				}
			}
		};
		fastThreadPool.setName("MTH");
		fastThreadPool.refreshThreadCount();

		IBeanConfiguration fastThreadPoolBean = beanContextFactory.registerExternalBean(THREAD_POOL_NAME, fastThreadPool);

		beanContextFactory.registerDisposable(fastThreadPool);

		beanContextFactory.registerBean(MultithreadingHelper.class).autowireable(IMultithreadingHelper.class)//
				.propertyRef(fastThreadPoolBean);

	}
}