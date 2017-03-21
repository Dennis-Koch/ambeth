package com.koch.ambeth.security.privilege.factory;

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
import com.koch.ambeth.security.privilege.model.impl.DefaultPrivilegeImpl;

public class DefaultEntityPrivilegeFactory implements IEntityPrivilegeFactory {
	@Override
	public IPrivilege createPrivilege(boolean create, boolean read, boolean update, boolean delete,
			boolean execute, IPropertyPrivilege[] primitivePropertyPrivileges,
			IPropertyPrivilege[] relationPropertyPrivileges) {
		return new DefaultPrivilegeImpl(create, read, update, delete, execute,
				primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
