package com.koch.ambeth.security.privilege;

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

import java.util.List;

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPrivilegeResult;
import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePrivilegeResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;

public interface IPrivilegeProviderIntern extends IPrivilegeProvider
{
	IPrivilegeCache createPrivilegeCache();

	IPrivilege getPrivilege(Object entity, ISecurityScope[] securityScopes);

	IPrivilege getPrivilegeByObjRef(IObjRef objRef, ISecurityScope[] securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IPrivilegeResult getPrivileges(List<?> entities, ISecurityScope[] securityScopes);

	/**
	 * Result correlates by-index with the given objRefs
	 * 
	 * @param objRefs
	 * @param securityScopes
	 * @return
	 */
	IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs, ISecurityScope[] securityScopes);

	ITypePrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope[] securityScopes);

	ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes, ISecurityScope[] securityScopes);

	IPrivilege getPrivilegeByObjRef(ObjRef objRef, IPrivilegeCache privilegeCache);

	IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs, IPrivilegeCache privilegeCache);
}
