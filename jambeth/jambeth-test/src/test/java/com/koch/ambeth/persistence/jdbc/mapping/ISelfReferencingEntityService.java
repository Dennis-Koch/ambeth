package com.koch.ambeth.persistence.jdbc.mapping;

/*-
 * #%L
 * jambeth-test
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.persistence.jdbc.mapping.models.SelfReferencingEntity;

public interface ISelfReferencingEntityService {
	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByName(String name);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByNames(String... names);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByNames(List<String> names);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity getSelfReferencingEntityByNames(Collection<String> names);

	@Cached(alternateIdName = "Name")
	SelfReferencingEntity[] getSelfReferencingEntitiesByNamesReturnArray(String... names);

	@Cached(type = SelfReferencingEntity.class, alternateIdName = "Name")
	List<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnList(String... names);

	@Cached(type = SelfReferencingEntity.class, alternateIdName = "Name")
	Set<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnSet(String... names);

	@Cached(type = SelfReferencingEntity.class, alternateIdName = "Name")
	Collection<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnCollection(
			String... names);

	void updateSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity);

	void deleteSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity);
}
