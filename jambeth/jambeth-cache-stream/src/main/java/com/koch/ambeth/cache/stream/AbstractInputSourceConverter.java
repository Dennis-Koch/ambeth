package com.koch.ambeth.cache.stream;

/*-
 * #%L
 * jambeth-cache-stream
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
