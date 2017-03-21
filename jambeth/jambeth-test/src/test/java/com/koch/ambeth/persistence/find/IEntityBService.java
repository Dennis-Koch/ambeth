package com.koch.ambeth.persistence.find;

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

import java.util.List;

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.QueryResultType;

@Service(IEntityBService.class)
public interface IEntityBService {
	@Cached
	Entity retrieve(int id);

	@Cached
	List<Entity> retrieve(List<Integer> ids);

	IPagingResponse<Entity> findReferences(IPagingRequest pagingRequest,
			IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.REFERENCES, referenceIdName = Entity.ALTERNATE_ID)
	IPagingResponse<Entity> findReferencesAlternate(IPagingRequest pagingRequest,
			IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.ENTITIES)
	IPagingResponse<Entity> findEntities(IPagingRequest pagingRequest,
			IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.BOTH)
	IPagingResponse<Entity> findBoth(IPagingRequest pagingRequest,
			IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);
}
