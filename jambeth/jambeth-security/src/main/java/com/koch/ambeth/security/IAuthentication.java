package com.koch.ambeth.security;

/*-
 * #%L
 * jambeth-security
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

/**
 * The basic credentials to be used to process the authentication step
 */
public interface IAuthentication {
	/**
	 * The unique name of the user to check
	 *
	 * @return The unique name of the user to check
	 */
	String getUserName();

	/**
	 * The secret of the given user name. Depending on the {@link #getType()} the secret may be hashed
	 * or plain depending on the implementation of the {@link IAuthenticationManager} to process this
	 * credentials
	 *
	 * @return The secret of the given user name
	 */
	char[] getPassword();

	/**
	 * Describes how the secret is to be treated
	 * 
	 * @return One of the following enumerated values: {@link PasswordType#PLAIN},
	 *         {@link PasswordType#MD5}, {@link PasswordType#SHA1}
	 */
	PasswordType getType();
}
