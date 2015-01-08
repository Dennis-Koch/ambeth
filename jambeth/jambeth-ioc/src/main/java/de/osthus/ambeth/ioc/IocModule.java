package de.osthus.ambeth.ioc;

import java.io.File;
import java.util.regex.Pattern;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.converter.StringToClassArrayConverter;
import de.osthus.ambeth.converter.StringToDoubleArrayConverter;
import de.osthus.ambeth.converter.StringToFileConverter;
import de.osthus.ambeth.converter.StringToFloatArrayConverter;
import de.osthus.ambeth.converter.StringToIntArrayConverter;
import de.osthus.ambeth.converter.StringToLongArrayConverter;
import de.osthus.ambeth.converter.StringToPatternConverter;
import de.osthus.ambeth.converter.StringToStringArrayConverter;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.AppendableStringBuilderCollectableController;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;
import de.osthus.ambeth.proxy.CgLibUtil;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.threading.FastThreadPool;
import de.osthus.ambeth.threading.GuiThreadHelper;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.util.DedicatedConverterUtil;
import de.osthus.ambeth.util.IInterningFeature;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.InterningFeature;
import de.osthus.ambeth.util.MultithreadingHelper;
import de.osthus.ambeth.util.converter.BooleanArrayConverter;
import de.osthus.ambeth.util.converter.ByteArrayConverter;
import de.osthus.ambeth.util.converter.CharArrayConverter;

@FrameworkModule
public class IocModule implements IInitializingModule
{
	public static final String THREAD_POOL_NAME = "threadPool";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("booleanArrayConverter", BooleanArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "booleanArrayConverter", String.class, boolean[].class);

		beanContextFactory.registerBean("byteArrayConverter", ByteArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "byteArrayConverter", String.class, byte[].class);

		beanContextFactory.registerBean("charArrayConverter", CharArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "charArrayConverter", String.class, char[].class);

		IBeanConfiguration stringToFileConverter = beanContextFactory.registerBean(StringToFileConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToFileConverter, String.class, File.class);
		DedicatedConverterUtil.biLink(beanContextFactory, stringToFileConverter, String.class, File[].class);

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

		beanContextFactory.registerBean("guiThreadHelper", GuiThreadHelper.class).autowireable(IGuiThreadHelper.class);

		FastThreadPool fastThreadPool = new FastThreadPool(0, Integer.MAX_VALUE, 60000)
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

		IBeanConfiguration fastThreadPoolBean = beanContextFactory.registerExternalBean(THREAD_POOL_NAME, fastThreadPool);

		beanContextFactory.registerBean(MultithreadingHelper.class).autowireable(IMultithreadingHelper.class)//
				.propertyRef(fastThreadPoolBean);

	}
}
