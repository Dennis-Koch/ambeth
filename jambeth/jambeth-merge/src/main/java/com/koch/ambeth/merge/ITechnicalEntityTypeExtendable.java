package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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

/**
 * This extendable enables Ambeth to match the technical entities of an application to the internal entities of Ambeth. An example for this is the IAuditEntry.
 * Inside of the application the entity is defined to match the database, e.g. AuditEntry and this is then mapped to the IAuditEntry to tell ambeth how to find
 * meta data for this entity.
 */
public interface ITechnicalEntityTypeExtendable
{
	void registerTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType);

	void unregisterTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType);

	Class<?> getEntityTypeForTechnicalEntity(Class<?> technicalEntitiyType);
}
