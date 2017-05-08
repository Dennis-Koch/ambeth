package com.koch.ambeth.persistence.jdbc.alternateid;

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

public interface IAlternateIdEntityService {
	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByName(String name);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByNames(String... names);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByNames(List<String> names);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity getAlternateIdEntityByNames(Collection<String> names);

	@Cached(alternateIdName = "Name")
	AlternateIdEntity[] getAlternateIdEntitiesByNamesReturnArray(String... names);

	@Cached(type = AlternateIdEntity.class, alternateIdName = "Name")
	List<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnList(String... names);

	@Cached(type = AlternateIdEntity.class, alternateIdName = "Name")
	Set<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnSet(String... names);

	@Cached(type = AlternateIdEntity.class, alternateIdName = "Name")
	Collection<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnCollection(String... names);

	void updateAlternateIdEntity(AlternateIdEntity alternateIdEntity);

	void deleteAlternateIdEntity(AlternateIdEntity alternateIdEntity);
}
