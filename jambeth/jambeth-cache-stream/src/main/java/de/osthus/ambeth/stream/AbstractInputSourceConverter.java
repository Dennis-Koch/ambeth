package de.osthus.ambeth.stream;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.IDedicatedConverter;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractInputSourceConverter implements IDedicatedConverter, IInitializingBean
{
	@Autowired
	protected IServiceContext beanContext;

	protected String chunkProviderName;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(chunkProviderName, "chunkProviderName");
	}

	public void setChunkProviderName(String chunkProviderName)
	{
		this.chunkProviderName = chunkProviderName;
	}

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		AbstractInputSourceValueHolder vh = createValueHolderInstance();
		vh.setBeanContext(beanContext);
		vh.setChunkProviderName(chunkProviderName);
		try
		{
			vh.afterPropertiesSet();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return vh;
	}

	protected abstract AbstractInputSourceValueHolder createValueHolderInstance();
}
