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

import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.util.annotation.Interning;

/**
 * For each change of an entity property (during create or update of an entity) in the lifecycle of
 * each entity tracked with the audit trail a new instance of this will be created. So if you have a
 * specific entity instance with version '5' in your hand a valid audit trail presumably has at
 * least 5 {@link IAuditedEntity} instances. Within each of those audited entities there is enclosed
 * at least 1 instance of this or at least 1 instance of {@link IAuditedEntityRelationProperty}.
 */
@Audited(false)
public interface IAuditedEntityPrimitiveProperty {
	String Entity = "Entity";

	String Order = "Order";

	String Name = "Name";

	String NewValue = "NewValue";

	IAuditedEntity getEntity();

	int getOrder();

	@Interning // it can be assumed that the variance of distinct entity property names is limited
	String getName();

	String getNewValue();
}
