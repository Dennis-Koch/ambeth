package com.koch.ambeth.cache.stream;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
