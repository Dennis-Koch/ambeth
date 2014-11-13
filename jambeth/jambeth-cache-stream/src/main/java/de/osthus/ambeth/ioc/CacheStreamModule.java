package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.stream.AbstractInputSourceConverter;
import de.osthus.ambeth.stream.IInputSource;
import de.osthus.ambeth.stream.IInputSourceTemplate;
import de.osthus.ambeth.stream.bool.BooleanInputSourceConverter;
import de.osthus.ambeth.stream.bool.IBooleanInputSource;
import de.osthus.ambeth.stream.float32.FloatInputSourceConverter;
import de.osthus.ambeth.stream.float32.IFloatInputSource;
import de.osthus.ambeth.stream.float64.DoubleInputSourceConverter;
import de.osthus.ambeth.stream.float64.IDoubleInputSource;
import de.osthus.ambeth.stream.int32.IIntInputSource;
import de.osthus.ambeth.stream.int32.IntInputSourceConverter;
import de.osthus.ambeth.stream.int64.ILongInputSource;
import de.osthus.ambeth.stream.int64.LongInputSourceConverter;
import de.osthus.ambeth.stream.strings.IStringInputSource;
import de.osthus.ambeth.stream.strings.StringInputSourceConverter;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;

@FrameworkModule
public class CacheStreamModule implements IInitializingModule
{
	public static final String CHUNK_PROVIDER_NAME = "chunkProvider";

	@LogInstance
	private ILogger log;

	protected String chunkProviderName = CHUNK_PROVIDER_NAME;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (chunkProviderName == null)
		{
			if (log.isWarnEnabled())
			{
				log.warn("Chunked streaming feature disabled: No property 'ChunkProviderName' defined");
			}
			return;
		}
		registerInputSourceConverter(beanContextFactory, BooleanInputSourceConverter.class, IBooleanInputSource.class);
		registerInputSourceConverter(beanContextFactory, DoubleInputSourceConverter.class, IDoubleInputSource.class);
		registerInputSourceConverter(beanContextFactory, FloatInputSourceConverter.class, IFloatInputSource.class);
		registerInputSourceConverter(beanContextFactory, IntInputSourceConverter.class, IIntInputSource.class);
		registerInputSourceConverter(beanContextFactory, LongInputSourceConverter.class, ILongInputSource.class);
		registerInputSourceConverter(beanContextFactory, StringInputSourceConverter.class, IStringInputSource.class);
	}

	public void setChunkProviderName(String chunkProviderName)
	{
		this.chunkProviderName = chunkProviderName;
	}

	public IBeanConfiguration registerInputSourceConverter(IBeanContextFactory beanContextFactory, Class<? extends AbstractInputSourceConverter> converterType,
			Class<? extends IInputSource> inputSourceType)
	{
		IBeanConfiguration converterBC = beanContextFactory.registerBean(converterType).propertyValue("ChunkProviderName", chunkProviderName);
		beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(IInputSourceTemplate.class, inputSourceType);
		return converterBC;
	}
}
