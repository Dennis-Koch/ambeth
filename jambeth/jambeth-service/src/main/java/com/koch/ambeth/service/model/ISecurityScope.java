package com.koch.ambeth.service.model;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.util.annotation.XmlType;

/**
 * Allows to further customize the authorization evaluation logic according to time- or
 * state-dependent distinct functional roles the same user might impersonate. Means: The same user
 * with the same static set of entity group, entity role and entity permissions might 'see and work'
 * with data differently depending on the 'active role' the user might have. This can be used to
 * implement something like a 'my workspace' and 'team workspace' functionality implemented with a
 * single service, called by the same user, but returning different data sets according to this
 * applications' specific contextual 'workspace'. It is implementation dependent whether the concept
 * of this security scopes are considered at all.
 *
 * @see IPermissionRule
 * @see IAuthorizationManager
 */
@XmlType
public interface ISecurityScope {
	/**
	 * The unique name of the contextual security scope. A straight-forward example could be
	 * 'MY_WORKSPACE' or 'TEAM_WORKSPACE'.
	 *
	 * @return Unique name of the contextual security scope
	 */
	String getName();
}
