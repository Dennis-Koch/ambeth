package de.osthus.ambeth.helloworld.service;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.filter.IFilterToQueryBuilder;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.helloworld.transfer.TestEntity2;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.util.ParamChecker;

@Service(name = "HelloWorldService", value = IHelloWorldService.class)
@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class HelloWorldService implements IHelloWorldService, IInitializingBean
{

	protected IQueryBuilderFactory qbf;

	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(qbf, "qbf");
		ParamChecker.assertNotNull(filterToQueryBuilder, "filterToQueryBuilder");
	}

	public void setQbf(IQueryBuilderFactory qbf)
	{
		this.qbf = qbf;
	}

	public void setFtqb(IFilterToQueryBuilder filterToQueryBuilder)
	{
		this.filterToQueryBuilder = filterToQueryBuilder;
	}

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
