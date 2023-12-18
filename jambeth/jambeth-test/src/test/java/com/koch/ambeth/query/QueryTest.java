package com.koch.ambeth.query;

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

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.filter.PagingRequest;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.exception.BeanAlreadyDisposedException;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.config.QueryConfigurationConstants;
import com.koch.ambeth.query.jdbc.sql.SqlColumnOperand;
import com.koch.ambeth.query.jdbc.sql.SqlJoinOperator;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IDataItem;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.JoinType;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertiesList({
        @TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc.connection.LogPreparedStatementInterceptor", value = "DEBUG"),
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/query/Query_orm.xml")
})
@SQLStructure("Query_structure.sql")
@SQLData("Query_data.sql")
@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class QueryTest extends AbstractInformationBusWithPersistenceTest {
    protected static final String paramName1 = "param.1";
    protected static final String paramName2 = "param.2";
    protected static final String columnName1 = "ID";

    protected static final String columnName2 = "VERSION";

    protected static final String columnName3 = "FK";
    protected static final String propertyName3 = "Fk";

    protected static final String propertyName5 = "Name1";

    protected static void assertSimilar(List<Integer> expectedIds, List<QueryEntity> actual) {
        assertNotNull(actual);
        assertEquals(expectedIds.size(), actual.size());
        for (int i = actual.size(); i-- > 0; ) {
            assertThat(expectedIds.contains(actual.get(i).getId())).describedAs("%s not expected", actual.get(i).getId()).isTrue();
        }
    }

    @Autowired
    protected ICache cache;
    @Autowired
    protected IMergeProcess mergeProcess;
    protected IQueryBuilder<QueryEntity> qb;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        qb = queryBuilderFactory.create(QueryEntity.class);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFinalize() throws Exception {
        IOperand rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));
        assertNotNull(qb.build(rootOperand));
    }

    @Test(expected = BeanAlreadyDisposedException.class)
    public void testFinalize_alreadBuild1() throws Exception {
        qb.build();
        qb.dispose();
        qb.build();
    }

    @SuppressWarnings("deprecation")
    @Test(expected = BeanAlreadyDisposedException.class)
    public void testFinalize_alreadyBuild2() throws Exception {
        IOperand rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));
        qb.build(rootOperand);
        qb.dispose();
        qb.build(rootOperand);
    }

    @SuppressWarnings("deprecation")
    @Test(expected = BeanAlreadyDisposedException.class)
    public void testFinalize_alreadyBuild3() throws Exception {
        IOperand rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));
        qb.build(rootOperand, new ISqlJoin[0]);
        qb.dispose();
        qb.build(rootOperand);
    }

    @SuppressWarnings("deprecation")
    @Test(expected = PersistenceException.class)
    public void testRetrieveAsVersionCursorNoTransaction() throws Exception {
        Object value1 = Integer.valueOf(3);

        IOperand rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));

        IQuery<QueryEntity> query = qb.build(rootOperand);

        IVersionCursor actual = query.param(paramName1, value1).retrieveAsVersions();
        assertNotNull(actual);
        Iterator<IVersionItem> iterator = actual.iterator();
        assertTrue(iterator.hasNext());
        IVersionItem item = iterator.next();
        assertNotNull(item);
        assertEquals(value1, conversionHelper.convertValueToType(value1.getClass(), item.getId()));
    }

    @Test
    public void testRetrieveAsVersionCursor() throws Exception {
        transaction.processAndCommit(new DatabaseCallback() {

            @Override
            public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception {
                testRetrieveAsVersionCursorNoTransaction();
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRetrieveAsEntityCursor() throws Exception {
        var value1 = Integer.valueOf(2);

        var rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));

        var query = qb.build(rootOperand);

        transaction.runInTransaction(() -> {
            var actual = query.param(paramName1, value1).retrieveAsCursor();
            assertThat(actual).isNotNull();
            try {
                var iterator = actual.iterator();
                assertThat(iterator.hasNext()).isTrue();
                var item = iterator.next();
                assertThat(item).isNotNull();
                assertThat(item.getId()).isEqualTo(value1);
            } finally {
                actual.dispose();
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRetrieveAsList() throws Exception {
        var values = Arrays.asList(new Integer[] { 2, 4 });

        var operand1 = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));
        var operand2 = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName2));
        var rootOperand = qb.or(operand1, operand2);

        var query = qb.build(rootOperand);

        var actual = query.param(paramName1, values.get(0)).param(paramName2, values.get(1)).retrieve();
        assertSimilar(values, actual);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void retrieveWhereNullByEqual() throws Exception {
        var expected = Arrays.asList(new Integer[] { 2, 5 });

        var query = qb.build(qb.let(qb.property(propertyName3)).isEqualTo(qb.parameterValue(paramName1)));

        var actual = query.param(paramName1, null).retrieve();
        assertSimilar(expected, actual);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void retrieveWhereNotNullByEqual() throws Exception {
        var expected = Arrays.asList(new Integer[] { 1, 3, 4, 6 });

        var query = qb.build(qb.let(qb.property(propertyName3)).isNotEqualTo(qb.parameterValue(paramName1)));

        var actual = query.param(paramName1, null).retrieve();
        assertSimilar(expected, actual);
    }

    /**
     * Greater-than with a null-value is always false - so no results.
     *
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @Test
    public void retrieveWhereGTNullByEqual() throws Exception {
        var query = qb.build(qb.let(qb.property(propertyName3)).isGreaterThan(qb.parameterValue(paramName1)));

        var actual = query.param(paramName1, null).retrieve();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void retrieveWhereNull() throws Exception {
        var expected = Arrays.asList(2, 5);

        var query = qb.build(qb.let(qb.property(propertyName3)).isNull());

        var actual = query.param(paramName1, null).retrieve();
        assertSimilar(expected, actual);
    }

    @Test
    public void retrieveWhereNotNull() throws Exception {
        var expected = Arrays.asList(1, 3, 4, 6);

        var query = qb.build(qb.let(qb.property(propertyName3)).isNotNull());

        var actual = query.param(paramName1, null).retrieve();
        assertSimilar(expected, actual);
    }

    @Test
    public void retrieveByDate_long() {
        var updatedOn = updateQueryEntity1();

        var query = qb.build(qb.let(qb.property(qb.plan().getUpdatedOn())).isEqualTo(qb.value(updatedOn)));
        var res = query.retrieve();
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(1, res.get(0).getId());
    }

    @Test
    public void retrieveByDate_Date() {
        var updatedOn = updateQueryEntity1();
        var updatedOnDate = new java.util.Date(updatedOn);

        var query = qb.build(qb.let(qb.property(qb.plan().getUpdatedOn())).isEqualTo(qb.value(updatedOnDate)));
        var res = query.retrieve();
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(1, res.get(0).getId());
    }

    @Test
    public void retrieveByDate_SqlDate() {
        var updatedOn = updateQueryEntity1();
        var updatedOnDate = new Date(updatedOn);

        var query = qb.build(qb.let(qb.property(qb.plan().getUpdatedOn())).isEqualTo(qb.value(updatedOnDate)));
        var res = query.retrieve();
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(1, res.get(0).getId());
    }

    @Test
    public void retrieveByDate_Timestamp() {
        var updatedOn = updateQueryEntity1();
        var timestamp = new Timestamp(updatedOn);

        var query = qb.build(qb.let(qb.property(qb.plan().getUpdatedOn())).isEqualTo(qb.value(timestamp)));
        var res = query.retrieve();
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(1, res.get(0).getId());
    }

    protected long updateQueryEntity1() {
        var queryEntity1 = cache.getObject(QueryEntity.class, 1);
        queryEntity1.setContent(2.);
        mergeProcess.process(queryEntity1);

        var updatedOn = queryEntity1.getUpdatedOn();
        return updatedOn;
    }

    @Test
    public void retrieveAll() throws Exception {
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);

        IQuery<QueryEntity> query = qb.build();
        List<QueryEntity> actual = query.retrieve();
        assertSimilar(expected, actual);
    }

    @Test
    public void retrieveWithLimitAndOrder1() throws Exception {
        IQuery<QueryEntity> query = qb.limit(qb.value(1)).orderBy(qb.property("UpdatedOn"), OrderByType.DESC).build();
        List<QueryEntity> actual = query.retrieve();
        assertEquals(1, actual.size());
    }

    @Test
    public void retrieveWithLimit1() throws Exception {
        IQuery<QueryEntity> query = qb.limit(qb.value(1)).build();
        List<QueryEntity> actual = query.retrieve();
        assertEquals(1, actual.size());
    }

    @Test
    public void retrieveSingleWithLimit1() throws Exception {
        IQuery<QueryEntity> query = qb.limit(qb.value(1)).build();
        QueryEntity actual = query.retrieveSingle();
        assertNotNull(actual);
    }

    @Test
    public void retrieveWithLimit100() throws Exception {
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);

        IQuery<QueryEntity> query = qb.limit(qb.value(100)).build();
        List<QueryEntity> actual = query.retrieve();
        assertSimilar(expected, actual);
    }

    @Test
    public void isEmptyWithLimit0() throws Exception {
        // This test makes sure that limit 1 is stil used for isEmpty
        IQuery<QueryEntity> query = qb.limit(qb.value(0)).build();
        Assert.assertFalse(query.isEmpty());
    }

    @Test
    public void retrieveAllAfterUpdate() throws Exception {
        transaction.processAndCommit(new DatabaseCallback() {
            @Override
            public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception {
                String name1Value = "name1xx";
                List<Integer> expectedBeforeUpdate = Arrays.asList(1, 2, 3, 4, 5, 6);
                List<Integer> expectedAfterUpdate = Arrays.asList(1, 3, 4, 5, 6);

                IQuery<QueryEntity> query = qb.build(qb.let(qb.property(QueryEntity.Name1)).isNotEqualTo(qb.value(name1Value)));
                List<QueryEntity> allBeforeUpdate = query.retrieve();
                assertSimilar(expectedBeforeUpdate, allBeforeUpdate);

                QueryEntity changedQueryEntity = beanContext.getService(ICache.class).getObject(QueryEntity.class, 2);
                changedQueryEntity.setName1(name1Value);
                beanContext.getService(IMergeProcess.class).process(changedQueryEntity);

                List<QueryEntity> allAfterUpdate = query.retrieve();
                assertSimilar(expectedAfterUpdate, allAfterUpdate);
            }
        });
    }

    @Test
    @PersistenceContext
    public void retrieveGroupByWithSum() throws Exception {
        // Select col1, sum(col2) as col2 from table group by col1 where col2 = ?
        // Select VERSION, sum(CONTENT) as CONTENT from QUERY_ENTITY group by VERSION
        var versionProperty = qb.property(qb.plan().getVersion());
        var versionIndex = qb.select(versionProperty);
        var contentIndex = qb.select(qb.function("SUM", qb.property(qb.plan().getContent())));
        var query = qb.groupBy(versionProperty).build();
        var dataCursor = query.retrieveAsData();
        try {
            for (var dataItem : dataCursor) {
                var version = dataItem.getValue(versionIndex);
                var content = dataItem.getValue(contentIndex);

                System.out.println(version + ": " + content);
            }
        } finally {
            dataCursor.dispose();
        }
    }

    @Test
    @PersistenceContext
    public void retrieveGroupByWithCount() throws Exception {
        // Select col1, sum(col2) as col2 from table group by col1 where col2 = ?
        // Select VERSION, count(VERSION) as COUNT from QUERY_ENTITY group by VERSION where VERSION = 2
        IOperand versionProperty = qb.property(qb.plan().getVersion());
        int versionIndex = qb.select(versionProperty);
        int countIndex = qb.select(qb.function("Count", versionProperty));
        qb.groupBy(versionProperty);
        IQuery<QueryEntity> query = qb.groupBy(versionProperty).build(qb.let(versionProperty).isEqualTo(qb.value(2)));
        IDataCursor dataCursor = query.retrieveAsData();
        try {
            for (IDataItem dataItem : dataCursor) {
                Object version = dataItem.getValue(versionIndex);
                Object content = dataItem.getValue(countIndex);
                System.out.println(version + ": " + content);
            }
        } finally {
            dataCursor.dispose();
        }
    }

    @Test
    @PersistenceContext
    public void retrieveAllGroupByOrderBy() throws Exception {
        IOperand versionOp = qb.property(qb.plan().getVersion());
        IOperand maxName1 = qb.function("MAX", qb.property(QueryEntity.Name1));
        int maxIndex = qb.select(maxName1);
        int versionIndex = qb.select(versionOp);

        IQuery<QueryEntity> query = qb.groupBy(versionOp).orderBy(versionOp, OrderByType.DESC).build(); // .build(qb.isNotEqualTo(maxName1,
        // qb.value(0)));

        Object[][] expected = { { 2, "name2" }, { 1, "name3" } };
        IDataCursor dataCursor = query.retrieveAsData();
        try {
            int index = 0;
            if (expected.length > 0) {
                Assert.assertEquals(expected[0].length, dataCursor.getFieldCount());
            }
            for (IDataItem dataItem : dataCursor) {
                Object version = dataItem.getValue(versionIndex);
                Object max = dataItem.getValue(maxIndex);
                Object[] expectedItem = expected[index];
                Assert.assertEquals(expectedItem[0].toString(), version.toString());
                Assert.assertEquals(expectedItem[1].toString(), max.toString());
                index++;
            }
            Assert.assertEquals(expected.length, index);
        } finally {
            dataCursor.dispose();
        }
    }

    @Test
    @TestProperties(name = QueryConfigurationConstants.PagingPrefetchBehavior, value = "true")
    public void retrievePagingAfterUpdate() throws Exception {
        transaction.runInTransaction(() -> {
            var name1Value = "name1xx";
            var expectedBeforeUpdate = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });
            var expectedAfterUpdate = Arrays.asList(new Integer[] { 1, 3, 4, 5, 6 });

            var pr = new PagingRequest().withNumber(1).withSize(expectedBeforeUpdate.size());
            var query = qb.buildPaging(qb.let(qb.property(qb.plan().getName1())).isNotEqualTo(qb.value(name1Value)));
            var allBeforeUpdate = query.retrieve(pr);
            assertSimilar(expectedBeforeUpdate, allBeforeUpdate.getResult());

            var changedQueryEntity = beanContext.getService(ICache.class).getObject(QueryEntity.class, 2);
            changedQueryEntity.setName1(name1Value);
            beanContext.getService(IMergeProcess.class).process(changedQueryEntity);

            var allAfterUpdate = query.retrieve(pr);
            assertSimilar(expectedAfterUpdate, allAfterUpdate.getResult());
        });
    }

    @SuppressWarnings("deprecation")
    @Test
    public void fulltextSimple() throws Exception {
        var rootOperand = qb.fulltext(qb.parameterValue(paramName1));
        qb.orderBy(qb.property(qb.plan().getId()), OrderByType.ASC);
        var query = qb.build(rootOperand);

        var result = query.param(paramName1, "me3").retrieve();
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(3, result.get(1).getId());
        assertEquals(4, result.get(2).getId());
    }

    @Test
    public void retrieveAllOrdered() throws Exception {
        var expectedIds = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });

        qb.orderBy(qb.property(qb.plan().getId()), OrderByType.ASC);
        var queryAsc = qb.build(qb.all());
        qb = queryBuilderFactory.create(QueryEntity.class);
        qb.orderBy(qb.property(qb.plan().getId()), OrderByType.DESC);
        var queryDesc = qb.build(qb.all());

        var actualAsc = queryAsc.retrieve();
        assertEquals(expectedIds.size(), actualAsc.size());
        for (int i = actualAsc.size(); i-- > 0; ) {
            assertEquals((int) expectedIds.get(i), actualAsc.get(i).getId());
        }

        var actualDesc = queryDesc.retrieve();
        var size = actualAsc.size();
        assertEquals(size, actualDesc.size());
        for (int i = size; i-- > 0; ) {
            assertEquals(actualAsc.get(i), actualDesc.get(size - i - 1));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void retrievePagingSimple() throws Exception {
        var expected = Arrays.asList(new Integer[] { 4, 3, 2 });

        qb.orderBy(qb.property(qb.plan().getId()), OrderByType.DESC);
        qb.orderBy(qb.property(qb.plan().getVersion()), OrderByType.ASC);
        var query = qb.build(qb.all());

        var actual = query.param(QueryConstants.PAGING_INDEX_OBJECT, 2).param(QueryConstants.PAGING_SIZE_OBJECT, expected.size()).retrieve();
        assertEquals(expected.size(), actual.size());
        for (int i = expected.size(); i-- > 0; ) {
            assertEquals((int) expected.get(i), actual.get(i).getId());
        }
    }

    /**
     * The column of the alternate key is selected two times (once as AK and once for the 'orderBy').
     *
     * @throws Exception
     */
    @Test
    public void retrievePagingAllOrderedByAK() throws Exception {
        var expectedIds = Arrays.asList(2, 1, 5, 6, 3, 4);

        qb.orderBy(qb.property("Name1"), OrderByType.ASC);
        var pagingQuery = qb.buildPaging(qb.all());

        var pagingRequest = new PagingRequest();
        pagingRequest.setNumber(1);
        pagingRequest.setSize(expectedIds.size());

        var pagingResponse = pagingQuery.retrieveRefs(pagingRequest);
        assertEquals(pagingRequest.getSize(), pagingResponse.getSize());

        var refResult = pagingResponse.getRefResult();
        assertEquals(pagingRequest.getSize(), refResult.size());
    }

    @PersistenceContext
    @Test
    public void retrieveAllOrderedByNotSelectedChildProp() throws Exception {
        var expectedIds = Arrays.asList(3, 6, 4, 1, 5, 2);

        // Query used:
        // SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME1",J_A."ID" FROM "JAMBETH"."QUERY_ENTITY" S_A
        // LEFT OUTER JOIN "JAMBETH"."JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
        // ORDER BY J_A."ID" ASC

        qb.orderBy(qb.property("Fk.Id"), OrderByType.ASC);
        var query = qb.build(qb.all());

        var versionCursor = query.retrieveAsVersions(true);
        try {
            var loadedIds = new ArrayList<Integer>();
            for (var current : versionCursor) {
                var currentId = conversionHelper.convertValueToType(Integer.class, current.getId());
                loadedIds.add(currentId);
            }
            var expectedArray = expectedIds.toArray();
            Arrays.sort(expectedArray);
            var loadedArray = loadedIds.toArray();
            Arrays.sort(loadedArray);
            Assert.assertArrayEquals(expectedArray, loadedArray);
        } finally {
            versionCursor.dispose();
        }
    }

    @Test
    public void testJoinQuery() throws Exception {
        var expectedIds = Arrays.asList(1, 4);

        // Query used:
        // SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME1" FROM "JAMBETH"."QUERY_ENTITY"
        // S_A LEFT OUTER JOIN "JAMBETH"."JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
        // WHERE (J_A."VERSION"=?)

        var query = qb.build(qb.let(qb.property("Fk.Version")).isEqualTo(qb.parameterValue(paramName1)));

        var actual = query.param(paramName1, 3).retrieve();
        assertSimilar(expectedIds, actual);

        actual = query.param(paramName1, 2).retrieve();
        assertNotNull(actual);
        assertEquals(2, actual.size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testJoinQueryWithOrderBy() throws Exception {
        var expectedIds = Arrays.asList(new Integer[] { 1, 4 });

        // Query used:
        // SELECT "QUERY_ENTITY"."ID","QUERY_ENTITY"."VERSION" FROM "QUERY_ENTITY"
        // LEFT OUTER JOIN "JOIN_QUERY_ENTITY" ON ("QUERY_ENTITY"."FK"="JOIN_QUERY_ENTITY"."ID")
        // WHERE ("JOIN_QUERY_ENTITY"."VERSION"=3)

        var fkA = qb.column(columnName3);
        var idB = qb.column(columnName1);
        var joinClause = qb.join(JoinQueryEntity.class, fkA, idB, JoinType.LEFT);

        var verB = qb.column(columnName2, joinClause);
        var whereClause = qb.let(verB).isEqualTo(qb.parameterValue(paramName1));

        qb.orderBy(qb.property(propertyName5), OrderByType.ASC);

        var query = qb.build(whereClause, joinClause);

        var actual = query.param(paramName1, 3).retrieve();
        assertSimilar(expectedIds, actual);

        actual = query.param(paramName1, 2).retrieve();
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertEquals(6, actual.get(0).getId());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testJoinToSelfQuery() throws Exception {
        var expectedIds = Arrays.asList(new Integer[] { 1, 3, 5, 6 });

        // Query used:
        // SELECT A."ID",A."VERSION" FROM "QUERY_ENTITY" A
        // LEFT OUTER JOIN "QUERY_ENTITY" B ON ((A."VERSION"=B."VERSION") AND (A."ID"<>B."ID"))
        // LEFT OUTER JOIN "JOIN_QUERY_ENTITY" C ON (B."FK"=C."ID")
        // WHERE (C."VERSION"=2);

        var idQE1 = qb.column(columnName1);
        var idQE2 = qb.column(columnName1);
        var verQE1 = qb.column(columnName2);
        var verQE2 = qb.column(columnName2);
        var joinClause1 = qb.join(QueryEntity.class, qb.and(qb.let(verQE1).isEqualTo(verQE2), qb.let(idQE1).isNotEqualTo(idQE2)), JoinType.LEFT);
        ((SqlColumnOperand) idQE2).setJoinClause((SqlJoinOperator) joinClause1);
        ((SqlColumnOperand) verQE2).setJoinClause((SqlJoinOperator) joinClause1);
        var fkQE = qb.column(columnName3, joinClause1);
        var idJQE = qb.column(columnName1);
        var joinClause2 = qb.join(JoinQueryEntity.class, fkQE, idJQE, JoinType.LEFT);

        var verJQE = qb.column(columnName2, joinClause2);
        var whereClause = qb.let(verJQE).isEqualTo(qb.parameterValue(paramName1));

        var query = qb.build(whereClause, joinClause1, joinClause2);

        var actual = query.param(paramName1, 2).retrieve();
        assertSimilar(expectedIds, actual);
    }

    @Test
    public void testJoinToSelfQueryByProperty() throws Exception {
        // SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME1" FROM "QUERY_ENTITY" S_A
        // LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_A ON (S_A."FK"=J_A."ID")
        // LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."PARENT"=J_B."ID")
        // WHERE (J_B."ID"=?)
        List<Integer> expectedIds = Arrays.asList(new Integer[] { 3, 6 });

        IOperand whereClause = qb.let(qb.property("Fk.Parent.Id")).isEqualTo(qb.parameterValue(paramName1));

        IQuery<QueryEntity> query = qb.build(whereClause);// whereClause, joinClause1, joinClause2);

        List<QueryEntity> actual = query.param(paramName1, 2).retrieve();
        assertSimilar(expectedIds, actual);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIsInMoreThan1000() throws Exception {
        int count = 2013;
        var entities = entityFactory.createEntity(QueryEntity.class, count);
        int index = 0;
        for (var entity : entities) {
            index++;
            entity.setName1("Name11_" + index);
            entity.setName2("Name22_" + index);
        }
        beanContext.getService(IMergeProcess.class).process(entities);
        var values = new ArrayList<>(count);
        for (int a = entities.size(); a-- > 0; ) {
            values.add(entities.get(a).getId());
        }
        var rootOperand = qb.let(qb.column(columnName1)).isIn(qb.parameterValue(paramName1));

        var query = qb.build(rootOperand);

        beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());

        entities = query.param(paramName1, values).retrieve();
        assertNotNull(entities);
        assertEquals(count, entities.size());
    }

    @Test
    public void testQueryKeyUniqueness() throws Exception {
        var query = qb.build(qb.or(qb.let(qb.property(qb.plan().getId())).isEqualTo(qb.value(3)), qb.let(qb.property(qb.plan().getFk().getVersion())).isEqualTo(qb.value(3))));
        var queryKey = ((IQueryIntern) query).getQueryKey(new HashMap<>());

        qb = queryBuilderFactory.create(QueryEntity.class);
        var query2 = qb.build(qb.or(qb.let(qb.property(qb.plan().getFk().getId())).isEqualTo(qb.value(3)), qb.let(qb.property(qb.plan().getVersion())).isEqualTo(qb.value(3))));
        var queryKey2 = ((IQueryIntern) query2).getQueryKey(new HashMap<>());

        assertFalse(queryKey.equals(queryKey2));
    }
}
