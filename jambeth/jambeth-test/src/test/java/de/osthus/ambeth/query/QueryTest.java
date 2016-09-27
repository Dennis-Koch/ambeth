package de.osthus.ambeth.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.JoinType;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.PagingRequest;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.exception.BeanAlreadyDisposedException;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.AbstractEntity;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDataItem;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.PersistenceContextType;
import de.osthus.ambeth.query.config.QueryConfigurationConstants;
import de.osthus.ambeth.query.sql.SqlColumnOperand;
import de.osthus.ambeth.query.sql.SqlJoinOperator;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({ @TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.connection.LogPreparedStatementInterceptor", value = "DEBUG"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/Query_orm.xml") })
@SQLStructure("Query_structure.sql")
@SQLData("Query_data.sql")
@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class QueryTest extends AbstractInformationBusWithPersistenceTest
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

	@Autowired
	protected ICache cache;

	@Autowired
	protected IMergeProcess mergeProcess;

	protected IQueryBuilder<QueryEntity> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		qb = queryBuilderFactory.create(QueryEntity.class);
		nameToValueMap.clear();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testFinalize() throws Exception
	{
		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		assertNotNull(qb.build(rootOperand));
	}

	@Test(expected = BeanAlreadyDisposedException.class)
	public void testFinalize_alreadBuild1() throws Exception
	{
		qb.build();
		qb.dispose();
		qb.build();
	}

	@SuppressWarnings("deprecation")
	@Test(expected = BeanAlreadyDisposedException.class)
	public void testFinalize_alreadyBuild2() throws Exception
	{
		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		qb.build(rootOperand);
		qb.dispose();
		qb.build(rootOperand);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = BeanAlreadyDisposedException.class)
	public void testFinalize_alreadyBuild3() throws Exception
	{
		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		qb.build(rootOperand, new ISqlJoin[0]);
		qb.dispose();
		qb.build(rootOperand);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testRetrieveAsVersionCursorNoTransaction() throws Exception
	{
		Object value1 = Integer.valueOf(3);

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));

		IQuery<QueryEntity> query = qb.build(rootOperand);

		IVersionCursor actual = query.param(paramName1, value1).retrieveAsVersions();
		assertNotNull(actual);
		assertTrue(actual.moveNext());
		assertNotNull(actual.getCurrent());
		assertEquals(value1, conversionHelper.convertValueToType(value1.getClass(), actual.getCurrent().getId()));
	}

	@Test
	public void testRetrieveAsVersionCursor() throws Exception
	{
		transaction.processAndCommit(new DatabaseCallback()
		{

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				testRetrieveAsVersionCursorNoTransaction();
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testRetrieveAsEntityCursor() throws Exception
	{
		final Object value1 = Integer.valueOf(2);

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));

		final IQuery<QueryEntity> query = qb.build(rootOperand);

		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(de.osthus.ambeth.collections.ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				IEntityCursor<QueryEntity> actual = query.param(paramName1, value1).retrieveAsCursor();
				assertNotNull(actual);
				assertTrue(actual.moveNext());
				assertNotNull(actual.getCurrent());
				assertEquals(value1, actual.getCurrent().getId());
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testRetrieveAsList() throws Exception
	{
		List<Integer> values = Arrays.asList(new Integer[] { 2, 4 });

		IOperand operand1 = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		IOperand operand2 = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName2));
		IOperand rootOperand = qb.or(operand1, operand2);

		IQuery<QueryEntity> query = qb.build(rootOperand);

		nameToValueMap.put(paramName1, values.get(0));
		nameToValueMap.put(paramName2, values.get(1));
		List<QueryEntity> actual = query.retrieve(nameToValueMap);
		assertSimilar(values, actual);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void retrieveWhereNullByEqual() throws Exception
	{
		List<Integer> expected = Arrays.asList(new Integer[] { 2, 5 });

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName3), qb.valueName(paramName1)));

		nameToValueMap.put(paramName1, null);
		List<QueryEntity> actual = query.retrieve(nameToValueMap);
		assertSimilar(expected, actual);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void retrieveWhereNotNullByEqual() throws Exception
	{
		List<Integer> expected = Arrays.asList(new Integer[] { 1, 3, 4, 6 });

		IQuery<QueryEntity> query = qb.build(qb.isNotEqualTo(qb.property(propertyName3), qb.valueName(paramName1)));

		nameToValueMap.put(paramName1, null);
		List<QueryEntity> actual = query.retrieve(nameToValueMap);
		assertSimilar(expected, actual);
	}

	/**
	 * Greater-than with a null-value is always false - so no results.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void retrieveWhereGTNullByEqual() throws Exception
	{
		IQuery<QueryEntity> query = qb.build(qb.isGreaterThan(qb.property(propertyName3), qb.valueName(paramName1)));

		nameToValueMap.put(paramName1, null);
		List<QueryEntity> actual = query.retrieve(nameToValueMap);
		assertTrue(actual.isEmpty());
	}

	@Test
	public void retrieveWhereNull() throws Exception
	{
		List<Integer> expected = Arrays.asList(2, 5);

		IQuery<QueryEntity> query = qb.build(qb.isNull(qb.property(propertyName3)));

		List<QueryEntity> actual = query.param(paramName1, null).retrieve();
		assertSimilar(expected, actual);
	}

	@Test
	public void retrieveWhereNotNull() throws Exception
	{
		List<Integer> expected = Arrays.asList(1, 3, 4, 6);

		IQuery<QueryEntity> query = qb.build(qb.isNotNull(qb.property(propertyName3)));

		List<QueryEntity> actual = query.param(paramName1, null).retrieve();
		assertSimilar(expected, actual);
	}

	@Test
	public void retrieveByDate_long()
	{
		long updatedOn = updateQueryEntity1();

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property("UpdatedOn"), qb.value(updatedOn)));
		IList<QueryEntity> res = query.retrieve();
		Assert.assertNotNull(res);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(1, res.get(0).getId());
	}

	@Test
	public void retrieveByDate_Date()
	{
		long updatedOn = updateQueryEntity1();
		java.util.Date updatedOnDate = new java.util.Date(updatedOn);

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property("UpdatedOn"), qb.value(updatedOnDate)));
		IList<QueryEntity> res = query.retrieve();
		Assert.assertNotNull(res);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(1, res.get(0).getId());
	}

	@Test
	public void retrieveByDate_SqlDate()
	{
		long updatedOn = updateQueryEntity1();
		Date updatedOnDate = new Date(updatedOn);

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property("UpdatedOn"), qb.value(updatedOnDate)));
		IList<QueryEntity> res = query.retrieve();
		Assert.assertNotNull(res);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(1, res.get(0).getId());
	}

	@Test
	public void retrieveByDate_Timestamp()
	{
		long updatedOn = updateQueryEntity1();
		Timestamp timestamp = new Timestamp(updatedOn);

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property("UpdatedOn"), qb.value(timestamp)));
		IList<QueryEntity> res = query.retrieve();
		Assert.assertNotNull(res);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(1, res.get(0).getId());
	}

	protected long updateQueryEntity1()
	{
		QueryEntity queryEntity1 = cache.getObject(QueryEntity.class, 1);
		queryEntity1.setContent(2.);
		mergeProcess.process(queryEntity1, null, null, null);

		long updatedOn = queryEntity1.getUpdatedOn();
		return updatedOn;
	}

	@Test
	public void retrieveAll() throws Exception
	{
		List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);

		IQuery<QueryEntity> query = qb.build();
		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expected, actual);
	}

	@Test
	public void retrieveWithLimitAndOrder1() throws Exception
	{
		IQuery<QueryEntity> query = qb.limit(qb.value(1)).orderBy(qb.property("UpdatedOn"), OrderByType.DESC).build();
		List<QueryEntity> actual = query.retrieve();
		assertEquals(1, actual.size());
	}

	@Test
	public void retrieveWithLimit1() throws Exception
	{
		IQuery<QueryEntity> query = qb.limit(qb.value(1)).build();
		List<QueryEntity> actual = query.retrieve();
		assertEquals(1, actual.size());
	}

	@Test
	public void retrieveSingleWithLimit1() throws Exception
	{
		IQuery<QueryEntity> query = qb.limit(qb.value(1)).build();
		QueryEntity actual = query.retrieveSingle();
		assertNotNull(actual);
	}

	@Test
	public void retrieveWithLimit100() throws Exception
	{
		List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);

		IQuery<QueryEntity> query = qb.limit(qb.value(100)).build();
		List<QueryEntity> actual = query.retrieve();
		assertSimilar(expected, actual);
	}

	@Test
	public void isEmptyWithLimit0() throws Exception
	{
		// This test makes sure that limit 1 is stil used for isEmpty
		IQuery<QueryEntity> query = qb.limit(qb.value(0)).build();
		Assert.assertFalse(query.isEmpty());
	}

	@Test
	public void retrieveAllAfterUpdate() throws Exception
	{
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				String name1Value = "name1xx";
				List<Integer> expectedBeforeUpdate = Arrays.asList(1, 2, 3, 4, 5, 6);
				List<Integer> expectedAfterUpdate = Arrays.asList(1, 3, 4, 5, 6);

				IQuery<QueryEntity> query = qb.build(qb.isNotEqualTo(qb.property(QueryEntity.Name1), qb.value(name1Value)));
				List<QueryEntity> allBeforeUpdate = query.retrieve();
				assertSimilar(expectedBeforeUpdate, allBeforeUpdate);

				QueryEntity changedQueryEntity = beanContext.getService(ICache.class).getObject(QueryEntity.class, 2);
				changedQueryEntity.setName1(name1Value);
				beanContext.getService(IMergeProcess.class).process(changedQueryEntity, null, null, null);

				List<QueryEntity> allAfterUpdate = query.retrieve();
				assertSimilar(expectedAfterUpdate, allAfterUpdate);
			}
		});
	}

	@Test
	@PersistenceContext
	public void retrieveGroupByWithSum() throws Exception
	{
		// Select col1, sum(col2) as col2 from table group by col1 where col2 = ?
		// Select VERSION, sum(CONTENT) as CONTENT from QUERY_ENTITY group by VERSION
		IOperand versionProperty = qb.property("Version");
		int versionIndex = qb.select(versionProperty);
		int contentIndex = qb.select(qb.function("SUM", qb.property("Content")));
		IQuery<QueryEntity> query = qb.groupBy(versionProperty).build();
		IDataCursor dataCursor = query.retrieveAsData();
		try
		{
			while (dataCursor.moveNext())
			{
				IDataItem dataItem = dataCursor.getCurrent();
				Object version = dataItem.getValue(versionIndex);
				Object content = dataItem.getValue(contentIndex);
				System.out.println(version + ": " + content);
			}
		}
		finally
		{
			dataCursor.dispose();
		}
	}

	@Test
	@PersistenceContext
	public void retrieveGroupByWithCount() throws Exception
	{
		// Select col1, sum(col2) as col2 from table group by col1 where col2 = ?
		// Select VERSION, count(VERSION) as COUNT from QUERY_ENTITY group by VERSION where VERSION = 2
		IOperand versionProperty = qb.property("Version");
		int versionIndex = qb.select(versionProperty);
		int countIndex = qb.select(qb.function("Count", versionProperty));
		qb.groupBy(versionProperty);
		IQuery<QueryEntity> query = qb.groupBy(versionProperty).build(qb.isEqualTo(versionProperty, qb.value(2)));
		IDataCursor dataCursor = query.retrieveAsData();
		try
		{
			while (dataCursor.moveNext())
			{
				IDataItem dataItem = dataCursor.getCurrent();
				Object version = dataItem.getValue(versionIndex);
				Object content = dataItem.getValue(countIndex);
				System.out.println(version + ": " + content);
			}
		}
		finally
		{
			dataCursor.dispose();
		}
	}

	@Test
	@PersistenceContext
	public void retrieveAllGroupByOrderBy() throws Exception
	{
		IOperand versionOp = qb.property(AbstractEntity.Version);
		IOperand maxName1 = qb.function("MAX", qb.property(QueryEntity.Name1));
		int maxIndex = qb.select(maxName1);
		int versionIndex = qb.select(versionOp);

		IQuery<QueryEntity> query = qb.groupBy(versionOp).orderBy(versionOp, OrderByType.DESC).build(); // .build(qb.isNotEqualTo(maxName1, qb.value(0)));

		Object[][] expected = { { 2, "name2" }, { 1, "name3" } };
		IDataCursor dataCursor = query.retrieveAsData();
		try
		{
			int index = 0;
			if (expected.length > 0)
			{
				Assert.assertEquals(expected[0].length, dataCursor.getFieldCount());
			}
			while (dataCursor.moveNext())
			{
				IDataItem dataItem = dataCursor.getCurrent();
				Object version = dataItem.getValue(versionIndex);
				Object max = dataItem.getValue(maxIndex);
				Object[] expectedItem = expected[index];
				Assert.assertEquals(expectedItem[0].toString(), version.toString());
				Assert.assertEquals(expectedItem[1].toString(), max.toString());
				index++;
			}
			Assert.assertEquals(expected.length, index);
		}
		finally
		{
			dataCursor.dispose();
		}
	}

	@Test
	@TestProperties(name = QueryConfigurationConstants.PagingPrefetchBehavior, value = "true")
	public void retrievePagingAfterUpdate() throws Exception
	{
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				String name1Value = "name1xx";
				List<Integer> expectedBeforeUpdate = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });
				List<Integer> expectedAfterUpdate = Arrays.asList(new Integer[] { 1, 3, 4, 5, 6 });

				PagingRequest pr = new PagingRequest().withSize(expectedBeforeUpdate.size());
				IPagingQuery<QueryEntity> query = qb.buildPaging(qb.isNotEqualTo(qb.property(QueryEntity.Name1), qb.value(name1Value)));
				IPagingResponse<QueryEntity> allBeforeUpdate = query.retrieve(pr);
				assertSimilar(expectedBeforeUpdate, allBeforeUpdate.getResult());

				QueryEntity changedQueryEntity = beanContext.getService(ICache.class).getObject(QueryEntity.class, 2);
				changedQueryEntity.setName1(name1Value);
				beanContext.getService(IMergeProcess.class).process(changedQueryEntity, null, null, null);

				IPagingResponse<QueryEntity> allAfterUpdate = query.retrieve(pr);
				assertSimilar(expectedAfterUpdate, allAfterUpdate.getResult());
			}
		});

	}

	@SuppressWarnings("deprecation")
	@Test
	public void fulltextSimple() throws Exception
	{
		IOperand rootOperand = qb.fulltext(qb.valueName(paramName1));
		qb.orderBy(qb.property("Id"), OrderByType.ASC);
		IQuery<QueryEntity> query = qb.build(rootOperand);

		HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();
		nameToValueMap.put(paramName1, "me3");
		List<QueryEntity> result = query.retrieve(nameToValueMap);
		assertEquals(3, result.size());
		assertEquals(1, result.get(0).getId());
		assertEquals(3, result.get(1).getId());
		assertEquals(4, result.get(2).getId());
	}

	@Test
	public void retrieveAllOrdered() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });

		qb.orderBy(qb.property("Id"), OrderByType.ASC);
		IQuery<QueryEntity> queryAsc = qb.build(qb.all());
		qb = queryBuilderFactory.create(QueryEntity.class);
		qb.orderBy(qb.property("Id"), OrderByType.DESC);
		IQuery<QueryEntity> queryDesc = qb.build(qb.all());

		List<QueryEntity> actualAsc = queryAsc.retrieve();
		assertEquals(expectedIds.size(), actualAsc.size());
		for (int i = actualAsc.size(); i-- > 0;)
		{
			assertEquals((int) expectedIds.get(i), actualAsc.get(i).getId());
		}

		List<QueryEntity> actualDesc = queryDesc.retrieve();
		int size = actualAsc.size();
		assertEquals(size, actualDesc.size());
		for (int i = size; i-- > 0;)
		{
			assertEquals(actualAsc.get(i), actualDesc.get(size - i - 1));
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void retrievePagingSimple() throws Exception
	{
		List<Integer> expected = Arrays.asList(new Integer[] { 4, 3, 2 });

		qb.orderBy(qb.property("Id"), OrderByType.DESC);
		qb.orderBy(qb.property("Version"), OrderByType.ASC);
		IQuery<QueryEntity> query = qb.build(qb.all());

		HashMap<Object, Object> currentNameToValueMap = new HashMap<Object, Object>(nameToValueMap);
		currentNameToValueMap.put(QueryConstants.PAGING_INDEX_OBJECT, 2);
		currentNameToValueMap.put(QueryConstants.PAGING_SIZE_OBJECT, expected.size());

		List<QueryEntity> actual = query.retrieve(currentNameToValueMap);
		assertEquals(expected.size(), actual.size());
		for (int i = expected.size(); i-- > 0;)
		{
			assertEquals((int) expected.get(i), actual.get(i).getId());
		}
	}

	/**
	 * Written to reproduce bug ticket https://jira.osthus.de/browse/AMBETH-498<br>
	 * The column of the alternate key is selected two times (once as AK and once for the 'orderBy').
	 * 
	 * @throws Exception
	 */
	@Test
	public void retrievePagingAllOrderedByAK() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(2, 1, 5, 6, 3, 4);

		qb.orderBy(qb.property("Name1"), OrderByType.ASC);
		IPagingQuery<QueryEntity> pagingQuery = qb.buildPaging(qb.all());

		PagingRequest pagingRequest = new PagingRequest();
		pagingRequest.setNumber(0);
		pagingRequest.setSize(expectedIds.size());

		IPagingResponse<QueryEntity> pagingResponse = pagingQuery.retrieveRefs(pagingRequest);
		assertEquals(pagingRequest.getSize(), pagingResponse.getSize());

		List<IObjRef> refResult = pagingResponse.getRefResult();
		assertEquals(pagingRequest.getSize(), refResult.size());
	}

	@PersistenceContext
	@Test
	public void retrieveAllOrderedByNotSelectedChildProp() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(3, 6, 4, 1, 5, 2);

		// Query used:
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME1",J_A."ID" FROM "JAMBETH"."QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "JAMBETH"."JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
		// ORDER BY J_A."ID" ASC

		qb.orderBy(qb.property("Fk.Id"), OrderByType.ASC);
		IQuery<QueryEntity> query = qb.build(qb.all());

		IVersionCursor versionCursor = query.retrieveAsVersions(true);
		try
		{
			int index = 0;
			while (versionCursor.moveNext())
			{
				IVersionItem current = versionCursor.getCurrent();
				Integer currentId = conversionHelper.convertValueToType(Integer.class, current.getId());
				Integer expectedId = expectedIds.get(index++);
				Assert.assertEquals(expectedId, currentId);
			}
			Assert.assertEquals(expectedIds.size(), index);
		}
		finally
		{
			versionCursor.dispose();
		}
	}

	@Test
	public void testJoinQuery() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(1, 4);

		// Query used:
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME1" FROM "JAMBETH"."QUERY_ENTITY"
		// S_A LEFT OUTER JOIN "JAMBETH"."JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
		// WHERE (J_A."VERSION"=?)

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property("Fk.Version"), qb.valueName(paramName1)));

		List<QueryEntity> actual = query.param(paramName1, 3).retrieve();
		assertSimilar(expectedIds, actual);

		actual = query.param(paramName1, 2).retrieve();
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals(6, actual.get(0).getId());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJoinQueryWithOrderBy() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(new Integer[] { 1, 4 });

		// Query used:
		// SELECT "QUERY_ENTITY"."ID","QUERY_ENTITY"."VERSION" FROM "QUERY_ENTITY"
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" ON ("QUERY_ENTITY"."FK"="JOIN_QUERY_ENTITY"."ID")
		// WHERE ("JOIN_QUERY_ENTITY"."VERSION"=3)

		IOperand fkA = qb.column(columnName3);
		IOperand idB = qb.column(columnName1);
		ISqlJoin joinClause = qb.join(JoinQueryEntity.class, fkA, idB, JoinType.LEFT);

		IOperand verB = qb.column(columnName2, joinClause);
		IOperand whereClause = qb.isEqualTo(verB, qb.valueName(paramName1));

		qb.orderBy(qb.property(propertyName5), OrderByType.ASC);

		IQuery<QueryEntity> query = qb.build(whereClause, joinClause);

		nameToValueMap.put(paramName1, 3);
		List<QueryEntity> actual = query.retrieve(nameToValueMap);
		assertSimilar(expectedIds, actual);

		nameToValueMap.clear();
		nameToValueMap.put(paramName1, 2);
		actual = query.retrieve(nameToValueMap);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals(6, actual.get(0).getId());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJoinToSelfQuery() throws Exception
	{
		List<Integer> expectedIds = Arrays.asList(new Integer[] { 1, 3, 5, 6 });

		// Query used:
		// SELECT A."ID",A."VERSION" FROM "QUERY_ENTITY" A
		// LEFT OUTER JOIN "QUERY_ENTITY" B ON ((A."VERSION"=B."VERSION") AND (A."ID"<>B."ID"))
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" C ON (B."FK"=C."ID")
		// WHERE (C."VERSION"=2);

		IOperand idQE1 = qb.column(columnName1);
		IOperand idQE2 = qb.column(columnName1);
		IOperand verQE1 = qb.column(columnName2);
		IOperand verQE2 = qb.column(columnName2);
		ISqlJoin joinClause1 = qb.join(QueryEntity.class, qb.and(qb.isEqualTo(verQE1, verQE2), qb.isNotEqualTo(idQE1, idQE2)), JoinType.LEFT);
		((SqlColumnOperand) idQE2).setJoinClause((SqlJoinOperator) joinClause1);
		((SqlColumnOperand) verQE2).setJoinClause((SqlJoinOperator) joinClause1);
		IOperand fkQE = qb.column(columnName3, joinClause1);
		IOperand idJQE = qb.column(columnName1);
		ISqlJoin joinClause2 = qb.join(JoinQueryEntity.class, fkQE, idJQE, JoinType.LEFT);

		IOperand verJQE = qb.column(columnName2, joinClause2);
		IOperand whereClause = qb.isEqualTo(verJQE, qb.valueName(paramName1));

		IQuery<QueryEntity> query = qb.build(whereClause, joinClause1, joinClause2);

		nameToValueMap.put(paramName1, 2);
		List<QueryEntity> actual = query.retrieve(nameToValueMap);
		assertSimilar(expectedIds, actual);
	}

	@Test
	public void testJoinToSelfQueryByProperty() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME1" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."PARENT"=J_B."ID")
		// WHERE (J_B."ID"=?)
		List<Integer> expectedIds = Arrays.asList(new Integer[] { 3, 6 });

		IOperand whereClause = qb.isEqualTo(qb.property("Fk.Parent.Id"), qb.valueName(paramName1));

		IQuery<QueryEntity> query = qb.build(whereClause);// whereClause, joinClause1, joinClause2);

		List<QueryEntity> actual = query.param(paramName1, 2).retrieve();
		assertSimilar(expectedIds, actual);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testIsInMoreThan1000() throws Exception
	{
		int count = 2013;
		IList<QueryEntity> entities = new ArrayList<QueryEntity>(count);
		for (int a = count; a-- > 0;)
		{
			QueryEntity queryEntity = entityFactory.createEntity(QueryEntity.class);
			queryEntity.setName1("Name11_" + a);
			queryEntity.setName2("Name22_" + a);
			entities.add(queryEntity);
		}
		beanContext.getService(IMergeProcess.class).process(entities, null, null, null);
		List<Object> values = new ArrayList<Object>(count);
		for (int a = entities.size(); a-- > 0;)
		{
			values.add(entities.get(a).getId());
		}
		IOperand rootOperand = qb.isIn(qb.column(columnName1), qb.valueName(paramName1));

		IQuery<QueryEntity> query = qb.build(rootOperand);

		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());

		entities = query.param(paramName1, values).retrieve();
		assertNotNull(entities);
		assertEquals(count, entities.size());
	}

	@Test
	public void testQueryKeyUniqueness() throws Exception
	{
		IQuery<QueryEntity> query = qb.build(qb.or(qb.isEqualTo(qb.property("Id"), qb.value(3)), qb.isEqualTo(qb.property("Fk.Version"), qb.value(3))));
		IQueryKey queryKey = query.getQueryKey(nameToValueMap);

		qb = queryBuilderFactory.create(QueryEntity.class);
		IQuery<QueryEntity> query2 = qb.build(qb.or(qb.isEqualTo(qb.property("Fk.Id"), qb.value(3)), qb.isEqualTo(qb.property("Version"), qb.value(3))));
		IQueryKey queryKey2 = query2.getQueryKey(nameToValueMap);

		assertFalse(queryKey.equals(queryKey2));
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
