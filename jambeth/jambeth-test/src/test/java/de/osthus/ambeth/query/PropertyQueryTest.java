package de.osthus.ambeth.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("PropertyQuery_data.sql")
@SQLStructure("PropertyQuery_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/Query_orm.xml")
public class PropertyQueryTest extends AbstractPersistenceTest
{
	protected static final String paramName1 = "param.1";
	protected static final String paramName2 = "param.2";
	protected static final String paramName3 = "param.3";
	protected static final String paramName4 = "param.4";

	protected static final String propertyName1 = "Id";
	protected static final String propertyName2 = "Version";
	protected static final String propertyName3 = "Fk.JoinValue1";
	protected static final String propertyName4 = "Fk.JoinValue2";
	protected static final String propertyName5 = "Fk.Parent.Version";
	protected static final String propertyName6 = "LinkTableEntity.Id";
	protected static final String propertyName7 = "Fk";

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	protected IQueryBuilder<QueryEntity> qb;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		qb = queryBuilderFactory.create(QueryEntity.class);
		nameToValueMap.clear();
	}

	@Test
	public void testSimpleQuery() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(2);

		// Query used:
		// SELECT "ID","VERSION" FROM "QUERY_ENTITY"
		// WHERE ("ID"=2)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName1), qb.value(expectedIds.get(0))));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testSimpleQuery_Context() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(2);

		// Query used:
		// SELECT "ID","VERSION" FROM "QUERY_ENTITY"
		// WHERE ("ID"=2)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName1), qb.value(expectedIds.get(0))));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testPropertyOverLinkTable() throws Exception
	{
		// Query used:
		// SELECT "ID","VERSION" FROM "QUERY_ENTITY"
		// WHERE ("ID"=2)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName6), qb.value(2)));

		List<QueryEntity> actuals = query.retrieve();
		assertEquals(1, actuals.size());
		QueryEntity actual = actuals.get(0);
		assertEquals(6, actual.getId());
		assertNotNull(actual.getLinkTableEntity());
		assertEquals(2, actual.getLinkTableEntity().getId());
	}

	@Test
	public void testMidRangeQuery() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(3, 4);

		// Query used:
		// SELECT "ID","VERSION" FROM "QUERY_ENTITY"
		// WHERE ((("VERSION"=2) AND (("ID"<>1) AND ("ID"<>5))) OR ("ID"=4))

		IOperand idProp = qb.property(propertyName1);
		IQuery<QueryEntity> query = qb
				.build(qb.or(
						qb.and(qb.isEqualTo(qb.property(propertyName2), qb.value(2)),
								qb.and(qb.isNotEqualTo(idProp, qb.value(1)), qb.isNotEqualTo(idProp, qb.value(5)))),
						qb.isEqualTo(idProp, qb.value(expectedIds.get(1)))));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoinQuery_notExistingProperty() throws Exception
	{
		qb.property("Property");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoinQuery_notExistingProperty2() throws Exception
	{
		qb.property("OtherEntity.Property");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJoinQuery_notExistingProperty3() throws Exception
	{
		qb.property("Fk.Property");
	}

	@Test
	public void testJoinQuery() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(1, 4);

		// Query used:
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
		// WHERE (J_A."JOIN_VALUE_1"=?)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName3), qb.valueName(paramName1)));

		List<QueryEntity> actual = query.param(paramName1, 3).retrieve();
		assertSimilar(expectedIds, actual);

		actual = query.param(paramName1, 2).retrieve();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(3, actual.get(0).getId());
	}

	@Test
	public void testJoinQuery2() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(2);

		// Query used:
		// SELECT DISTINCT A."ID",A."VERSION" FROM "QUERY_ENTITY" A
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" B ON (A."FK"=B."ID")
		// WHERE (((B."VERSION"=3) AND (B."ID"<>2)) OR (A."ID"=2))

		IQuery<QueryEntity> query = qb.build(qb.or(
				qb.and(qb.isEqualTo(qb.property(propertyName3), qb.value(3)), qb.isNotEqualTo(qb.property(propertyName4), qb.value(2))),
				qb.isEqualTo(qb.property(propertyName1), qb.value(2))));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testTwoJoinsQuery() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(3);

		// Query used:
		// SELECT A."ID",A."VERSION" FROM "QUERY_ENTITY" A
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" B ON (A."FK"=B."ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" C ON (B."PARENT"=C."ID")
		// WHERE (C."VERSION"=3)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName5), qb.value(3)));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testRelationByValue() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(1, 4);

		// Query used:
		// SELECT "ID","VERSION" FROM "QUERY_ENTITY" WHERE ("FK"=2)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName7), qb.value(2)));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testRelationByEntity() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(1, 4);

		// Query used:
		// SELECT "ID","VERSION" FROM "QUERY_ENTITY" WHERE ("FK"=2)

		JoinQueryEntity jqe = beanContext.getService(ICache.class).getObject(JoinQueryEntity.class, 2);
		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName7), qb.value(jqe)));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testAndList()
	{
		List<Integer> expectedIds = Arrays.asList(3);

		IQuery<QueryEntity> query = qb.build(qb.and(qb.isEqualTo(qb.property("Version"), qb.value(2)), qb.isEqualTo(qb.property("Id"), qb.value(3)),
				qb.isEqualTo(qb.property("Version"), qb.value(2))));

		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expectedIds, actual);
	}

	protected void assertSimilar(List<Integer> expectedIds, List<QueryEntity> actual)
	{
		assertNotNull(actual);
		assertEquals(expectedIds.size(), actual.size());
		for (int i = actual.size(); i-- > 0;)
		{
			assertTrue(expectedIds.contains(actual.get(i).getId()));
		}
	}
}
