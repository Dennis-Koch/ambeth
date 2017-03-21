package com.koch.ambeth.service.rest;

/*-
 * #%L
 * jambeth-service-rest
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
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

public class AuthenticationHolder implements IAuthenticationHolder, IInitializingBean {
	@Property(name = ServiceConfigurationConstants.UserName, mandatory = false,
			defaultValue = "dummyUser")
	protected String UserName;

	@Property(name = ServiceConfigurationConstants.Password, mandatory = false,
			defaultValue = "dummyPass")
	protected String Password;

	protected String[] auth;

	@Override
	public void afterPropertiesSet() throws Throwable {
		setAuthentication(UserName, Password);
	}

	@Override
	public String[] getAuthentication() {
		return auth.clone();
	}

	@Override
	public void setAuthentication(String userName, String password) {
		auth = new String[] {userName, password};
	}
}
