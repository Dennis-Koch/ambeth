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

import java.io.Serializable;

public class DefaultAuthentication implements IAuthentication, Serializable
{
	private static final long serialVersionUID = -5075984200275756231L;

	protected String userName;

	protected char[] userPass;

	protected PasswordType passwordType;

	public DefaultAuthentication()
	{
		// Intended blank
	}

	public DefaultAuthentication(String userName, char[] userPass, PasswordType passwordType)
	{
		this.userName = userName;
		this.userPass = userPass;
		this.passwordType = passwordType;
	}

	@Override
	public String getUserName()
	{
		return userName;
	}

	@Override
	public char[] getPassword()
	{
		return userPass;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public void setUserPass(char[] userPass)
	{
		this.userPass = userPass;
	}

	public void setPasswordType(PasswordType passwordType)
	{
		this.passwordType = passwordType;
	}

	@Override
	public PasswordType getType()
	{
		return passwordType;
	}
}
