package com.koch.ambeth.persistence.find;

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
public interface IEntityBService
{
	@Cached
	Entity retrieve(int id);

	@Cached
	List<Entity> retrieve(List<Integer> ids);

	IPagingResponse<Entity> findReferences(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.REFERENCES, referenceIdName = Entity.ALTERNATE_ID)
	IPagingResponse<Entity> findReferencesAlternate(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.ENTITIES)
	IPagingResponse<Entity> findEntities(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.BOTH)
	IPagingResponse<Entity> findBoth(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);
}
