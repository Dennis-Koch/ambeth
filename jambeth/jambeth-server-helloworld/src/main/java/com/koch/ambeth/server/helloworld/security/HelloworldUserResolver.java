package com.koch.ambeth.server.helloworld.security;

/*-
 * #%L
 * jambeth-server-helloworld
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.server.ISignatureUtil;
import com.koch.ambeth.security.server.IUserResolver;

public class HelloworldUserResolver implements IUserResolver {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Override
	public IUser resolveUserBySID(String sid) {
		PojoUser user = new PojoUser(sid);
		user.setPassword(createInMemoryPasswordBySID(user, sid));
		return user;
	}

	protected IPassword createInMemoryPasswordBySID(IUser user, String sid) {
		char[] clearTextPassword = sid.toCharArray();
		passwordUtil.assignNewPassword(clearTextPassword, user, null);
		signatureUtil.generateNewSignature(user.getSignature(), clearTextPassword);
		return user.getPassword();
	}
}
