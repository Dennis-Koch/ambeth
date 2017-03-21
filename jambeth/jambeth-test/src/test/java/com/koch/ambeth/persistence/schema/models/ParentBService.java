package com.koch.ambeth.persistence.schema.models;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.proxy.Service;

@Service(IParentBService.class)
public class ParentBService implements IParentBService
{
	@SuppressWarnings("unused")
	@LogInstance(ParentBService.class)
	private ILogger log;

	@Override
	public ParentB create(ParentB entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ParentB retrieve(int id)
	{
		return null;
	}

	@Override
	public ParentB update(ParentB entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(ParentB entity)
	{
		throw new UnsupportedOperationException();
	}
}
