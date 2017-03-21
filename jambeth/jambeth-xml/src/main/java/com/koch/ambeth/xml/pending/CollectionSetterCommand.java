package com.koch.ambeth.xml.pending;

/*-
 * #%L
 * jambeth-xml
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

import java.util.Collection;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.xml.IReader;

public class CollectionSetterCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean
{
	private Object object;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intentionally not calling super

		ParamChecker.assertTrue(objectFuture != null || object != null, "Either ObjectFuture or Object have to be set");
		ParamChecker.assertNotNull(parent, "Parent");
		ParamChecker.assertParamOfType(parent, "Parent", Collection.class);
	}

	public void setObject(Object object)
	{
		this.object = object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(IReader reader)
	{
		Object value = objectFuture != null ? objectFuture.getValue() : object;
		((Collection<Object>) parent).add(value);
	}
}
