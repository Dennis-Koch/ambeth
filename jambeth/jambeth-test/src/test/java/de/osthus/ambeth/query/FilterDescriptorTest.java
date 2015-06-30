package de.osthus.ambeth.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.filter.IFilterToQueryBuilder;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.model.FilterDescriptor;
import de.osthus.ambeth.filter.model.FilterOperator;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.filter.model.PagingRequest;
import de.osthus.ambeth.filter.model.SortDescriptor;
import de.osthus.ambeth.filter.model.SortDirection;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("FilterDescriptor_data.sql")
@SQLStructure("FilterDescriptor_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/FilterDescriptor_orm.xml")
public class FilterDescriptorTest extends AbstractInformationBusWithPersistenceTest
{
	protected static final String paramName1 = "param.1";
	protected static final String paramName2 = "param.2";
	protected static final String columnName1 = "ID";
	protected static final String propertyName1 = "Id";
	protected static final String columnName2 = "VERSION";
	protected static final String propertyName2 = "Version";
	protected static final String columnName3 = "FK";
	protected static final String propertyName3 = "Fk";
	protected static final String columnName4 = "CONTENT";
	protected static final String propertyName4 = "Content";
	protected static final String propertyName5 = "Name1";

	protected IQueryBuilder<QueryEntity> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	@Before
	public void setUp() throws Exception
	{
		qb = queryBuilderFactory.create(QueryEntity.class);
		nameToValueMap.clear();
	}

	@After
	public void tearDown() throws Exception
	{
		if (qb != null)
		{
			qb.dispose();
			qb = null;
		}
	}

	@Test
	public void retrievePagingSimpleWithDescriptor() throws Exception
	{
		IFilterToQueryBuilder ftqb = beanContext.getService(IFilterToQueryBuilder.class);

		List<Integer> expected = Arrays.asList(new Integer[] { 4, 3 });

		FilterDescriptor<QueryEntity> fd = new FilterDescriptor<QueryEntity>(QueryEntity.class);

		SortDescriptor sd1 = new SortDescriptor();
		sd1.setMember("Id");
		sd1.setSortDirection(SortDirection.DESCENDING);

		SortDescriptor sd2 = new SortDescriptor();
		sd2.setMember("Version");
		sd2.setSortDirection(SortDirection.ASCENDING);

		PagingRequest pReq = new PagingRequest();
		pReq.setNumber(1);
		pReq.setSize(expected.size());

		PagingRequest randomPReq = new PagingRequest();
		randomPReq.setNumber(1);
		randomPReq.setSize(2);
		IPagingQuery<QueryEntity> pagingQuery = ftqb.buildQuery(fd, new ISortDescriptor[] { sd1, sd2 });

		IPagingResponse<QueryEntity> response = pagingQuery.retrieve(randomPReq);
		// IPagingResponse<QueryEntity> response1_2 = pagingQuery.retrieve(randomPReq);

		// IPagingQuery<QueryEntity> pagingQuery2 = ftqb.buildQuery(fd, new ISortDescriptor[] { sd2, sd1 });

		// IPagingResponse<QueryEntity> response2 = pagingQuery2.retrieve(randomPReq);

		List<QueryEntity> result = response.getResult();

		// List<QueryEntity> result2 = response2.getResult();

		ArrayList<QueryEntity> qeResult = new ArrayList<QueryEntity>(result.size());
		for (int a = 0, size = result.size(); a < size; a++)
		{
			qeResult.add(result.get(a));
		}
		Assert.assertEquals(randomPReq.getSize(), qeResult.size());

		assertSimilar(expected, qeResult);
	}

	@Test
	public void retrievePagingWithEmptyResult() throws Exception
	{
		IFilterToQueryBuilder ftqb = beanContext.getService(IFilterToQueryBuilder.class);

		FilterDescriptor<QueryEntity> fd = new FilterDescriptor<QueryEntity>(QueryEntity.class);
		fd.setOperator(FilterOperator.IS_IN);
		fd.setMember("Id");
		fd.withValue("-1");

		PagingRequest pReq = new PagingRequest();
		pReq.setNumber(0);
		pReq.setSize(5);

		IPagingQuery<QueryEntity> pagingQuery = ftqb.buildQuery(fd, new ISortDescriptor[0]);

		IPagingResponse<QueryEntity> response = pagingQuery.retrieve(pReq);
		List<QueryEntity> result = response.getResult();

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void retrieveIsIn() throws Exception
	{
		IFilterToQueryBuilder ftqb = beanContext.getService(IFilterToQueryBuilder.class);

		List<Integer> expected = Arrays.asList(new Integer[] { 3 });

		FilterDescriptor<QueryEntity> fd = new FilterDescriptor<QueryEntity>(QueryEntity.class);
		fd.setOperator(FilterOperator.IS_IN);
		fd.setMember("Id");
		fd.withValue("4").withValue("3");

		SortDescriptor sd1 = new SortDescriptor();
		sd1.setMember("Id");
		sd1.setSortDirection(SortDirection.DESCENDING);

		SortDescriptor sd2 = new SortDescriptor();
		sd2.setMember("Version");
		sd2.setSortDirection(SortDirection.ASCENDING);

		PagingRequest pReq = new PagingRequest();
		pReq.setNumber(1);
		pReq.setSize(expected.size());

		PagingRequest randomPReq = new PagingRequest();
		randomPReq.setNumber(1);
		randomPReq.setSize(1);
		IPagingQuery<QueryEntity> pagingQuery = ftqb.buildQuery(fd, new ISortDescriptor[] { sd1, sd2 });

		IPagingResponse<QueryEntity> response = pagingQuery.retrieve(randomPReq);

		List<QueryEntity> result = response.getResult();

		ArrayList<QueryEntity> qeResult = new ArrayList<QueryEntity>(result.size());
		for (int a = 0, size = result.size(); a < size; a++)
		{
			qeResult.add(result.get(a));
		}
		Assert.assertEquals(randomPReq.getSize(), qeResult.size());

		assertSimilar(expected, qeResult);
	}

	@Test
	public void retrieveEmptyIsIn() throws Exception
	{
		IFilterToQueryBuilder ftqb = beanContext.getService(IFilterToQueryBuilder.class);

		List<Integer> expected = Arrays.asList(new Integer[] { 3 });

		FilterDescriptor<QueryEntity> fd = new FilterDescriptor<QueryEntity>(QueryEntity.class);
		fd.setOperator(FilterOperator.IS_IN);
		fd.setMember("Id");

		SortDescriptor sd1 = new SortDescriptor();
		sd1.setMember("Id");
		sd1.setSortDirection(SortDirection.DESCENDING);

		SortDescriptor sd2 = new SortDescriptor();
		sd2.setMember("Version");
		sd2.setSortDirection(SortDirection.ASCENDING);

		PagingRequest pReq = new PagingRequest();
		pReq.setNumber(1);
		pReq.setSize(expected.size());

		PagingRequest randomPReq = new PagingRequest();
		randomPReq.setNumber(1);
		randomPReq.setSize(1);
		IPagingQuery<QueryEntity> pagingQuery = ftqb.buildQuery(fd, new ISortDescriptor[] { sd1, sd2 });

		IPagingResponse<QueryEntity> response = pagingQuery.retrieve(randomPReq);

		List<QueryEntity> result = response.getResult();

		Assert.assertEquals(0, result.size());
	}

	/**
	 * JIRA Ticket AMBETH-321 describes a NullPointerException in NullValueOperand.expandQuery() when accessing the typeStack. JIRA Ticket AMBETH-322 describes
	 * a problem with field names with paging subselects that contain joins.
	 */
	@Test
	public void testForAMBETH321AndAMBETH322()
	{
		IFilterToQueryBuilder ftqb = beanContext.getService(IFilterToQueryBuilder.class);

		FilterDescriptor<QueryEntity> fd = new FilterDescriptor<QueryEntity>(QueryEntity.class);
		fd.setOperator(FilterOperator.IS_EQUAL_TO);
		fd.setMember("Fk.Version");

		SortDescriptor sd1 = new SortDescriptor();
		sd1.setMember("Id");
		sd1.setSortDirection(SortDirection.DESCENDING);

		PagingRequest pReq = new PagingRequest();
		pReq.setNumber(1);
		pReq.setSize(1);

		PagingRequest randomPReq = new PagingRequest();
		randomPReq.setNumber(1);
		randomPReq.setSize(1);
		IPagingQuery<QueryEntity> pagingQuery = ftqb.buildQuery(fd, new ISortDescriptor[] { sd1 });

		// If this does not throws an exception it is ok
		pagingQuery.retrieve(randomPReq);
	}

	protected static void assertSimilar(List<Integer> expectedIds, List<QueryEntity> actual)
	{
		assertNotNull(actual);
		assertEquals(expectedIds.size(), actual.size());
		for (int i = actual.size(); i-- > 0;)
		{
			assertTrue(actual.get(i).getId() + " not expected", expectedIds.contains(actual.get(i).getId()));
		}
	}
}
