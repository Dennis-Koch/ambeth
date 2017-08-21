package com.koch.ambeth.audit.model;

/*-
 * #%L
 * jambeth-audit
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

import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.annotation.Interning;

/**
 * Encapsulates audit information (audited entities or audited service invocations) made within a
 * single transaction. So whenever at least one audited entity is changed during a transaction - and
 * therefore an instance of {@link IAuditedEntity} is created - exactly one instance of this is
 * transparently created and all changes of the audited entity and any additional audited entities
 * are collected by this instance.<br>
 * <br>
 * For verification it is possible to verify this instance or a specific enclosed
 * {@link IAuditedEntity}. The latter is helpful to verify a complete lifecycle-chain of a specific
 * entity.
 */
@Audited(false)
public interface IAuditEntry {
	String Protocol = "Protocol";

	String Timestamp = "Timestamp";

	String User = "User";

	String UserIdentifier = "UserIdentifier";

	String Services = "Services";

	String SignatureOfUser = "SignatureOfUser";

	String Entities = "Entities";

	String Reason = "Reason";

	String Context = "Context";

	String HashAlgorithm = "HashAlgorithm";

	String SignedValue = "SignedValue";

	int getProtocol();

	long getTimestamp();

	IUser getUser();

	@Interning // it can be assumed that the variance of distinct user identifiers is limited
	String getUserIdentifier();

	String getReason();

	String getContext();

	/**
	 * Is is referred explicitly to the signature of a user because the relationship from the referred
	 * user to its signature may change over time. But the association to any historic
	 * {@link IAuditEntry} must be immutable to allow a consistent verification.
	 *
	 * @return The signature of the user which was valid at the time this audit entry has been created
	 *         and has been used to sign this audit entry
	 */
	ISignature getSignatureOfUser();

	char[] getSignedValue();

	@Interning // it can be assumed that the variance of distinct hash algorithms is limited
	String getHashAlgorithm();

	List<? extends IAuditedService> getServices();

	List<? extends IAuditedEntity> getEntities();
}
