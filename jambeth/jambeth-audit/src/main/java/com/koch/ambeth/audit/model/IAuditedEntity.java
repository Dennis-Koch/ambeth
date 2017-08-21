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
import com.koch.ambeth.util.annotation.Interning;

/**
 * For each change (insert/update/delete) in the lifecycle of each entity tracked with the audit
 * trail a new instance of this will be created. So if you have a specific entity instance with
 * version '5' in your hand a valid audit trail presumably has also exactly 5 instances of this (1
 * for the insert with version 1 and 4 for the updates increasing the version number from 1 to 5).
 * Each persisted valid instance of this encloses either at least 1
 * {@link IAuditedEntityPrimitiveProperty} or at least 1 {@link IAuditedEntityRelationProperty}.
 */
@Audited(false)
public interface IAuditedEntity {
	String ChangeType = "ChangeType";

	String Entry = "Entry";

	String Order = "Order";

	String Primitives = "Primitives";

	String Ref = "Ref";

	String RefPreviousVersion = "RefPreviousVersion";

	String Relations = "Relations";

	String SignedValue = "SignedValue";

	AuditedEntityChangeType getChangeType();

	IAuditEntry getEntry();

	int getOrder();

	List<? extends IAuditedEntityPrimitiveProperty> getPrimitives();

	IAuditedEntityRef getRef();

	/**
	 * The previous entity reference version is necessary to be safe against "inter-chain tampering":
	 * Means recognizing on verification the removal or addition of complete audited entity
	 * instances.<br>
	 * <br>
	 * This recognition should also be possible of the "inserted" {@link IAuditedEntity} itself is
	 * correctly signed together with its enclosing {@link IAuditEntry}: this "last-minute" entry
	 * tries to manipulate to causal dependency chain of a passed entity history and shall therefore
	 * be not allowed for regulatory concerns.
	 *
	 * @return The previous version of the entity reference describes by {@link #getRef()}
	 */
	@Interning // // it can be assumed that the variance of distinct versions is comparatively limited
	String getRefPreviousVersion();

	List<? extends IAuditedEntityRelationProperty> getRelations();

	char[] getSignedValue();
}
