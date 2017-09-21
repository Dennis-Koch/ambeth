package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server-test
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

import java.security.GeneralSecurityException;

import org.junit.Test;

public class PasswordTest // extends AbstractIocTest
{
	@Test
	public void testEffort() throws GeneralSecurityException {
		char[] password = "12345678".toCharArray();
		long startMillis = System.currentTimeMillis();
		byte[] saltBytes = new OnDemandSecureRandom().acquireRandomBytes(16);
		Passwords.hashPassword(password, saltBytes);
		System.out.println("time " + (System.currentTimeMillis() - startMillis) + "ms");
	}
}
