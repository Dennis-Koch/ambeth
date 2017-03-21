package com.koch.ambeth.security;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.IUserResolver;

public class TestUserResolver implements IUserResolver, IInitializingBean
{
	@Autowired
	protected ICache cache;

	@Autowired
	protected IUserIdentifierProvider userIdentifierProvider;

	protected String propertyNameOfSID;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		propertyNameOfSID = userIdentifierProvider.getPropertyNameOfSID();
	}

	@Override
	public IUser resolveUserBySID(String sid)
	{
		return cache.getObject(IUser.class, propertyNameOfSID, sid);
	}
}
