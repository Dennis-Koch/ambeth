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

public final class DenyAllPrivilege extends AbstractPrivilege {
	private static final long serialVersionUID = -8854111344355491831L;

	public static final IPrivilege INSTANCE = new DenyAllPrivilege();

	private static final IPropertyPrivilege denyAllPropertyPrivilege = PropertyPrivilegeImpl
			.create(false, false, false, false);

	private DenyAllPrivilege() {
		super(false, false, false, false, false, null, null);
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid() {
		return denyAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex) {
		return denyAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex) {
		return denyAllPropertyPrivilege;
	}

	@Override
	public boolean isCreateAllowed() {
		return false;
	}

	@Override
	public boolean isReadAllowed() {
		return false;
	}

	@Override
	public boolean isUpdateAllowed() {
		return false;
	}

	@Override
	public boolean isDeleteAllowed() {
		return false;
	}

	@Override
	public boolean isExecuteAllowed() {
		return false;
	}
}
