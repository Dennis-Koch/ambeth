package de.osthus.ambeth.persistence.find;

import java.util.List;

import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.QueryResultType;
import de.osthus.ambeth.cache.Cached;
import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;

@Service(IEntityBService.class)
@PersistenceContext
public interface IEntityBService
{
	@Cached
	Entity retrieve(int id);

	@Cached
	List<Entity> retrieve(List<Integer> ids);

	IPagingResponse<Entity> findReferences(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.ENTITIES)
	IPagingResponse<Entity> findEntities(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);

	@Find(resultType = QueryResultType.BOTH)
	IPagingResponse<Entity> findBoth(IPagingRequest pagingRequest, IFilterDescriptor<Entity> filterDescriptor, ISortDescriptor[] sortDescriptors);
}
