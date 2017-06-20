package com.koch.ambeth.security.server.privilege;

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

public class EntityTypePermissionRuleRemovedEvent {
	private final IEntityTypePermissionRule entityTypePermissionRule;

	private final Class<?> entityType;

	public EntityTypePermissionRuleRemovedEvent(IEntityTypePermissionRule entityTypePermissionRule,
			Class<?> entityType) {
		this.entityTypePermissionRule = entityTypePermissionRule;
		this.entityType = entityType;
	}

	public IEntityTypePermissionRule getEntityTypePermissionRule() {
		return entityTypePermissionRule;
	}

	public Class<?> getEntityType() {
		return entityType;
	}
}
