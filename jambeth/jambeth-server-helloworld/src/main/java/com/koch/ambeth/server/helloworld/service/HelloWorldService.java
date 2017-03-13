package com.koch.ambeth.server.helloworld.service;

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.server.helloworld.transfer.TestEntity;
import com.koch.ambeth.server.helloworld.transfer.TestEntity2;
import com.koch.ambeth.service.proxy.Service;

@Service(name = "HelloWorldService", value = IHelloWorldService.class)
@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class HelloWorldService implements IHelloWorldService
{
	@Autowired
	protected IQueryBuilderFactory qbf;

	@Autowired
	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public IPagingResponse<TestEntity> findTestEntities(IFilterDescriptor<TestEntity> filterDescriptor, ISortDescriptor[] sortDescriptors,
			IPagingRequest pagingRequest)
	{
		IPagingQuery<TestEntity> pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);

		return pagingQuery.retrieve(pagingRequest);
	}

	@Override
	@SecurityContext(SecurityContextType.AUTHENTICATED)
	public List<TestEntity> getAllTestEntities()
	{
		return qbf.create(TestEntity.class).build().retrieve();
	}

	@Override
	@SecurityContext(SecurityContextType.AUTHORIZED)
	public List<TestEntity2> getAllTest2Entities()
	{
		return qbf.create(TestEntity2.class).build().retrieve();
	}

	@Override
	public double doFunnyThings(int value, String text)
	{
		return ((value + text.length()) % 2) + 0.3456;
	}

	@Override
	public void saveTestEntities(TestEntity... testEntities)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void saveTestEntities(Collection<TestEntity> testEntities)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void saveTest2Entities(TestEntity2... test2Entities)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void deleteTestEntities(TestEntity... testEntities)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void deleteTestEntities(Collection<TestEntity> testEntities)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void deleteTestEntities(long... ids)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void deleteTest2Entities(TestEntity2... test2Entities)
	{
		throw new UnsupportedOperationException("Not implemented, because this should never be called");
	}

	@Override
	public void forbiddenMethod()
	{
		throw new IllegalStateException("This will never occur because security will catch this call");
	}
}
