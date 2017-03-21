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

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;

public final class SkipAllTypePrivilege extends AbstractTypePrivilege
{
	public static final ITypePrivilege INSTANCE = new SkipAllTypePrivilege();

	private static final ITypePropertyPrivilege skipAllPropertyPrivilege = TypePropertyPrivilegeImpl.create(null, null, null, null);

	private SkipAllTypePrivilege()
	{
		super(null, null, null, null, null, null, null);
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public Boolean isCreateAllowed()
	{
		return null;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return null;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return null;
	}

	@Override
	public Boolean isDeleteAllowed()
	{
		return null;
	}

	@Override
	public Boolean isExecuteAllowed()
	{
		return null;
	}
}
