package com.koch.ambeth.security.service;

import com.koch.ambeth.security.model.IUser;

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

import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ISecurityService {
	Object callServiceInSecurityScope(ISecurityScope[] securityScopes,
			IServiceDescription serviceDescription);

	boolean isSecured();

	IUser getCurrentUser();

	boolean currentUserHasActionPermission(String permission);

	boolean validatePassword(char[] newCleartextPassword);

	boolean changePassword(char[] newCleartextPassword);
}
