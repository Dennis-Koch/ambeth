package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server
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

import com.koch.ambeth.security.model.IUser;

/**
 * Extension which needs to be implemented in order to provide for Ambeth Security basic
 * functionalities to work with an unspecified {@link IUser} handle. In most implementations the
 * {@link IUser} handle is extended with a more concrete interface and this extended interface is
 * then mapped to a persisted entity in the database or an LDAP / Active Directory.
 */
public interface IUserIdentifierProvider {
	/**
	 * Returns the SID (unique string) identifying the given user. It is not necessarily a true
	 * Microsoft SID but the concept is derived from it.
	 *
	 * @param user
	 *          The user where the SID is to be retrieved from
	 * @return The retrieved SID of the given user
	 */
	String getSID(IUser user);

	/**
	 * Checks whether the given user is 'active' in the meaning of being allowed to be used for
	 * authentication and authorization requests. A deactivated user handle needs to be activated by
	 * whatever logic before it can be used for anything.
	 *
	 * @param user
	 *          The user which shall be checks for its active state.
	 * @return True if the user is active, false if not and any authentication/authorization request
	 *         shall be denied.
	 */
	boolean isActive(IUser user);

	/**
	 * Provides the name of the property to which the SID in mapped to. It may be the name 'SID' but
	 * may be any other property name.
	 *
	 * @return The name of the property to which the SID of the {@link IUser} handle is mapped to.
	 */
	String getPropertyNameOfSID();
}
