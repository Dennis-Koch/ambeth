package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.stream.GenericInputSourceConverter;
import de.osthus.ambeth.stream.IInputSource;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.stream.bool.BinaryBooleanConverter;
import de.osthus.ambeth.stream.bool.IBooleanInputSource;
import de.osthus.ambeth.stream.bool.IBooleanInputStream;
import de.osthus.ambeth.stream.float32.BinaryFloatConverter;
import de.osthus.ambeth.stream.float32.IFloatInputSource;
import de.osthus.ambeth.stream.float32.IFloatInputStream;
import de.osthus.ambeth.stream.float64.BinaryDoubleConverter;
import de.osthus.ambeth.stream.float64.IDoubleInputSource;
import de.osthus.ambeth.stream.float64.IDoubleInputStream;
import de.osthus.ambeth.stream.int32.BinaryIntConverter;
import de.osthus.ambeth.stream.int32.IIntInputSource;
import de.osthus.ambeth.stream.int32.IIntInputStream;
import de.osthus.ambeth.stream.int64.BinaryLongConverter;
import de.osthus.ambeth.stream.int64.ILongInputSource;
import de.osthus.ambeth.stream.int64.ILongInputStream;
import de.osthus.ambeth.stream.strings.BinaryStringConverter;
import de.osthus.ambeth.stream.strings.IStringInputSource;
import de.osthus.ambeth.stream.strings.IStringInputStream;
import de.osthus.ambeth.util.IDedicatedConverter;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;

@FrameworkModule
public class StreamModule implements IInitializingModule
{
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration genericConverterBC = beanContextFactory.registerAnonymousBean(GenericInputSourceConverter.class);
		beanContextFactory.link(genericConverterBC).to(IDedicatedConverterExtendable.class).with(IInputSource.class, IInputStream.class);

		registerConverter(beanContextFactory, BinaryBooleanConverter.class, boolean[].class, IBooleanInputStream.class, IBooleanInputSource.class);
		registerConverter(beanContextFactory, BinaryDoubleConverter.class, double[].class, IDoubleInputStream.class, IDoubleInputSource.class);
		registerConverter(beanContextFactory, BinaryFloatConverter.class, float[].class, IFloatInputStream.class, IFloatInputSource.class);
		registerConverter(beanContextFactory, BinaryIntConverter.class, int[].class, IIntInputStream.class, IIntInputSource.class);
		registerConverter(beanContextFactory, BinaryLongConverter.class, long[].class, ILongInputStream.class, ILongInputSource.class);
		registerConverter(beanContextFactory, BinaryStringConverter.class, String[].class, IStringInputStream.class, IStringInputSource.class);
	}

	public static IBeanConfiguration registerConverter(IBeanContextFactory beanContextFactory, Class<? extends IDedicatedConverter> type, Class<?> fromType,
			Class<? extends IInputStream> inputStreamType, Class<? extends IInputSource> inputSourceType)
	{
		IBeanConfiguration converterBC = beanContextFactory.registerAnonymousBean(type);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(fromType, IBinaryInputStream.class);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(fromType, inputStreamType);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(inputSourceType, inputStreamType);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(inputSourceType, IBinaryInputStream.class);
		return converterBC;
	}

}
