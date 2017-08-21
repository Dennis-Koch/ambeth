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
 * Describes a reference to a specific version of an entity which is audit trailed. It is used by
 * {@link IAuditedEntity} to describe the specific audited entity and used by
 * {@link IAuditedEntityRelationPropertyItem} to describe an added or removed entity relationship.
 * During the creation of a specific single {@link IAuditEntry} - so during a single transaction -
 * each participating entity instance is referred by a unique instance of this (means: this instance
 * is shared within a single potentially large transaction). But an instance of
 * {@link IAuditedEntityRef} is never shared amongst different audit entries.
 */
@Audited(false)
public interface IAuditedEntityRef {
	String EntityId = "EntityId";

	String EntityType = "EntityType";

	String EntityVersion = "EntityVersion";

	String getEntityId();

	@Interning // it can be assumed that the variance of distinct entity type names is limited
	String getEntityType();

	@Interning // it can be assumed that the variance of distinct versions is comparatively limited
	String getEntityVersion();
}
