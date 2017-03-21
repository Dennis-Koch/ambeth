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
import com.koch.ambeth.persistence.jdbc.mapping.models.OneToManyEntity;

public interface IOneToManyEntityService
{
	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByName(String name);

	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByNames(String... names);

	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByNames(List<String> names);

	@Cached(alternateIdName = "Name")
	OneToManyEntity getOneToManyEntityByNames(Collection<String> names);

	@Cached(alternateIdName = "Name")
	OneToManyEntity[] getOneToManyEntitiesByNamesReturnArray(String... names);

	@Cached(type = OneToManyEntity.class, alternateIdName = "Name")
	List<OneToManyEntity> getOneToManyEntitiesByNamesReturnList(String... names);

	@Cached(type = OneToManyEntity.class, alternateIdName = "Name")
	Set<OneToManyEntity> getOneToManyEntitiesByNamesReturnSet(String... names);

	@Cached(type = OneToManyEntity.class, alternateIdName = "Name")
	Collection<OneToManyEntity> getOneToManyEntitiesByNamesReturnCollection(String... names);

	void updateOneToManyEntity(OneToManyEntity oneToManyEntity);

	void deleteOneToManyEntity(OneToManyEntity oneToManyEntity);
}
