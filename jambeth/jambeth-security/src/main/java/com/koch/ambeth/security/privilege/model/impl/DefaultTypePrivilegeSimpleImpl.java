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

import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;

public class DefaultTypePrivilegeSimpleImpl extends AbstractTypePrivilege {
	protected final Boolean read, create, update, delete, execute;

	protected final ITypePropertyPrivilege propertyPrivilege;

	public DefaultTypePrivilegeSimpleImpl(Boolean create, Boolean read, Boolean update,
			Boolean delete, Boolean execute, ITypePropertyPrivilege propertyPrivilege) {
		super(create, read, update, delete, execute, null, null);
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
		this.propertyPrivilege = propertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex) {
		return propertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex) {
		return propertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid() {
		return propertyPrivilege;
	}

	@Override
	public Boolean isCreateAllowed() {
		return create;
	}

	@Override
	public Boolean isReadAllowed() {
		return read;
	}

	@Override
	public Boolean isUpdateAllowed() {
		return update;
	}

	@Override
	public Boolean isDeleteAllowed() {
		return delete;
	}

	@Override
	public Boolean isExecuteAllowed() {
		return execute;
	}
}
