package com.koch.ambeth.security.privilege.model.impl;

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

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilegeResult;

public class TypePrivilegeResult implements ITypePrivilegeResult, Serializable
{
	private static final long serialVersionUID = -5403054067546734382L;

	private final String sid;

	private final ITypePrivilege[] typePrivileges;

	public TypePrivilegeResult(String sid, ITypePrivilege[] typePrivileges)
	{
		this.sid = sid;
		this.typePrivileges = typePrivileges;
	}

	@Override
	public String getSID()
	{
		return sid;
	}

	@Override
	public ITypePrivilege[] getTypePrivileges()
	{
		return typePrivileges;
	}
}
