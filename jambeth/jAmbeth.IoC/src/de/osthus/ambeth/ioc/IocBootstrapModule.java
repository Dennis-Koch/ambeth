package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CgLibUtil;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.threading.GuiThreadHelper;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.util.DedicatedConverterUtil;
import de.osthus.ambeth.util.DelegatingConversionHelper;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;
import de.osthus.ambeth.util.IInterningFeature;
import de.osthus.ambeth.util.InterningFeature;
import de.osthus.ambeth.util.converter.BooleanArrayConverter;
import de.osthus.ambeth.util.converter.ByteArrayConverter;
import de.osthus.ambeth.util.converter.CharArrayConverter;

@FrameworkModule
public class IocBootstrapModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("conversionHelper", DelegatingConversionHelper.class).propertyRefs("*conversionHelper")
				.autowireable(IConversionHelper.class, IDedicatedConverterExtendable.class);

		beanContextFactory.registerBean("booleanArrayConverter", BooleanArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "booleanArrayConverter", String.class, boolean[].class);

		beanContextFactory.registerBean("byteArrayConverter", ByteArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "byteArrayConverter", String.class, byte[].class);

		beanContextFactory.registerBean("charArrayConverter", CharArrayConverter.class);
		DedicatedConverterUtil.biLink(beanContextFactory, "charArrayConverter", String.class, char[].class);

		beanContextFactory.registerBean("interningFeature", InterningFeature.class).autowireable(IInterningFeature.class);

		beanContextFactory.registerBean("cgLibUtil", CgLibUtil.class).autowireable(ICgLibUtil.class);

		beanContextFactory.registerBean("guiThreadHelper", GuiThreadHelper.class).autowireable(IGuiThreadHelper.class);
	}
}
