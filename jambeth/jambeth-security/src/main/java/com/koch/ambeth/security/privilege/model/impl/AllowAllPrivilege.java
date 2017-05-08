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

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;

public final class AllowAllPrivilege extends AbstractPrivilege {
	public static final IPrivilege INSTANCE = new AllowAllPrivilege();

	private static final IPropertyPrivilege allowAllPropertyPrivilege =
			PropertyPrivilegeImpl.create(true, true, true, true);

	private AllowAllPrivilege() {
		super(true, true, true, true, true, null, null);
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid() {
		return allowAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex) {
		return allowAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex) {
		return allowAllPropertyPrivilege;
	}

	@Override
	public boolean isCreateAllowed() {
		return true;
	}

	@Override
	public boolean isReadAllowed() {
		return true;
	}

	@Override
	public boolean isUpdateAllowed() {
		return true;
	}

	@Override
	public boolean isDeleteAllowed() {
		return true;
	}

	@Override
	public boolean isExecuteAllowed() {
		return true;
	}
}
