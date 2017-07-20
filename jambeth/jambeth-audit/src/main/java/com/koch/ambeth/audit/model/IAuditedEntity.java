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

@Audited(false)
public interface IAuditedEntity {
	public static final String ChangeType = "ChangeType";

	public static final String Entry = "Entry";

	public static final String Order = "Order";

	public static final String Primitives = "Primitives";

	public static final String Ref = "Ref";

	public static final String RefPreviousVersion = "RefPreviousVersion";

	public static final String Relations = "Relations";

	public static final String Signature = "Signature";

	AuditedEntityChangeType getChangeType();

	IAuditEntry getEntry();

	int getOrder();

	List<? extends IAuditedEntityPrimitiveProperty> getPrimitives();

	IAuditedEntityRef getRef();

	@Interning
	String getRefPreviousVersion();

	List<? extends IAuditedEntityRelationProperty> getRelations();

	char[] getSignature();
}
