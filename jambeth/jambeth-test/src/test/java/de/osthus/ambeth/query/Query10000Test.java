package de.osthus.ambeth.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/QueryMassdata_orm.xml") })
@SQLStructure("QueryMassdata_structure.sql")
@SQLData("Query10000_data.sql")
public class Query10000Test extends AbstractPersistenceTest
{
	protected static final String paramName1 = "param.1";

	protected IQueryBuilder<QueryEntity> qb;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "2000")
	public void testRetrieveFetch2000() throws Exception
	{
		IQuery<QueryEntity> query = qb.build(qb.like(qb.property("Name1"), qb.valueName(paramName1)));
		IList<QueryEntity> result = query.param(paramName1, "Q\\_%\\_Name%").retrieve();
		Assert.assertEquals(10000, result.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "1000")
	public void testRetrieveFetch1000() throws Exception
	{
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
		List<String> names = new ArrayList<String>(result.size());
		for (int a = result.size(); a-- > 0;)
		{
			names.add(result.get(a).getName1());
		}
		qb = queryBuilderFactory.create(QueryEntity.class);
		IQuery<QueryEntity> query2 = qb.build(qb.and(qb.isIn(qb.property("Name1"), qb.value(names)), qb.isEqualTo(qb.property("Version"), qb.value(3))));
		IList<QueryEntity> result2 = query2.retrieve();
		Assert.assertEquals(5000, result2.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "100")
	public void testRetrieveFetch100() throws Exception
	{
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "10")
	public void testRetrieveFetch10() throws Exception
	{
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
	}

	@Test
	@TestProperties(name = PersistenceConfigurationConstants.FetchSize, value = "1")
	public void testRetrieveFetch1() throws Exception
	{
		IQuery<QueryEntity> query = qb.build();
		IList<QueryEntity> result = query.retrieve();
		Assert.assertEquals(10004, result.size());
	}
}
