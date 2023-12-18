package com.koch.ambeth.query.sql;

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

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.QueryEntity;
import com.koch.ambeth.query.jdbc.StringQuery;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Stack;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/query/Query_orm.xml")
@SQLStructure("../Query_structure.sql")
@SQLData("../Query_data.sql")
public class SqlQueryTest extends AbstractInformationBusWithPersistenceTest {
    protected IQueryBuilder<?> qb;

    protected ArrayList<Object> parameters = new ArrayList<>();

    protected Stack<Class<?>> columnTypeStack = new Stack<>();

    protected String paramName1 = "param.1";
    protected String paramName2 = "param.2";

    protected String columnId = "ID";
    protected String columnVersion = "VERSION";
    protected String columnName1 = "NAME1";
    protected String columnName2 = "NAME2";
    protected String columnContent = "CONTENT";

    protected String propertyId = "Id";
    protected String propertyVersion = "Version";
    protected String propertyName1 = "Name1";
    protected String propertyName2 = "Name2";
    protected String propertyContent = "Content";

    @Before
    public void setUp() throws Exception {
        qb = queryBuilderFactory.create(QueryEntity.class);
    }

    @After
    public void tearDown() throws Exception {
        Assert.assertTrue(columnTypeStack.isEmpty());
        qb = null;
    }

    protected String buildQuery(IOperand rootOperand) {
        var query = beanContext.registerBean(StringQuery.class)//
                               .propertyValue("EntityType", Object.class)//
                               .propertyValue("RootOperand", rootOperand)//
                               .finish();
        var parameters = new ArrayList<>();

        return query.fillQuery(Map.of(), parameters);
    }

    protected String buildSimpleQuery(Object paramKey, Object value, IOperand rootOperand, List<Object> parameters) {
        var query = beanContext.registerBean(StringQuery.class)//
                               .propertyValue("EntityType", Object.class)//
                               .propertyValue("RootOperand", rootOperand)//
                               .finish();

        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return query.fillQuery(Map.of(paramKey, value), parameters);
    }

    protected String buildCompositeQuery(Object paramKey1, Object value1, Object paramKey2, Object value2, IOperand rootOperand, List<Object> parameters) {
        var query = beanContext.registerBean(StringQuery.class)//
                               .propertyValue("EntityType", Object.class)//
                               .propertyValue("RootOperand", rootOperand)//
                               .finish();

        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return query.fillQuery(Map.of(paramKey1, value1, paramKey2, value2), parameters);
    }

    @Test
    public void testDirectValue() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.property(propertyName1)).isEqualTo(qb.value(value1));
        var queryString = buildQuery(rootOperand);
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
    }

    @Test
    public void simpleSimpleValue() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.property(propertyId)).isEqualTo(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
        Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"=?)", queryString);
    }

    @Test
    public void escapeString() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.property(propertyName1)).isEqualTo(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlStartsWith() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).startsWith(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ?)", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlContains() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).contains(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')", queryString);
    }

    @Test
    public void sqlCount() throws Exception {
        var query = qb.build();
        Assert.assertEquals(6, query.count());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlEndsWith() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).endsWith(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ?)", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlGroupBy() throws Exception {
        qb.groupBy(qb.column(columnName1));

        var query = qb.build(qb.all());
        var queryKey = ((IQueryIntern) query).getQueryKey(null);
        Assert.assertEquals(QueryEntity.class.getName() + "###GROUP BY \"" + columnName1 + "\"#", queryKey.toString());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsContainedIn() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.parameterValue(paramName1)).isContainedIn(qb.column(columnName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsContainedInIS() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.parameterValue(paramName1)).isContainedIn(qb.column(columnName1), Boolean.FALSE);
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") LIKE LOWER(?) ESCAPE '\\')", queryString);
    }

    @Test
    public void sqlIsEmpty() throws Exception {
        var query = qb.build();
        Assert.assertFalse(query.isEmpty());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsEqualTo() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
    }

    @Test
    public void sqlIsGreaterThan() throws Exception {
        Double value1 = 55.0;

        var rootOperand = qb.let(qb.property(propertyContent)).isGreaterThan(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Double.class, parameters.get(0)));
        Assert.assertEquals("Wrong query string", "(\"" + columnContent + "\">?)", queryString);
    }

    @Test
    public void sqlIsGreaterThanOrEqualTo() throws Exception {
        Double value1 = 55.0;

        var rootOperand = qb.let(qb.property(propertyContent)).isGreaterThanOrEqualTo(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnContent + "\">=?)", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsIn() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).isIn(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" IN (?))", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsInIS() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).isIn(qb.parameterValue(paramName1), Boolean.FALSE);
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(value1.toString(), parameters.get(0));
        Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") IN (LOWER(?)))", queryString);
    }

    @Test
    public void sqlIsLessThan() throws Exception {
        Short value1 = (short) 55;

        var rootOperand = qb.let(qb.property(propertyId)).isLessThan(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Short.class, parameters.get(0)));
        Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"<?)", queryString);
    }

    @Test
    public void sqlIsLessThanOrEqualTo() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.property(propertyId)).isLessThanOrEqualTo(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
        Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"<=?)", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsNotContainedIn() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.parameterValue(paramName1)).isNotContainedIn(qb.column(columnName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsNotContainedInIS() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.parameterValue(paramName1)).isNotContainedIn(qb.column(columnName1), Boolean.FALSE);
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") NOT LIKE LOWER(?) ESCAPE '\\')", queryString);
    }

    @Test
    public void sqlIsNotEqualTo() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.property(propertyId)).isNotEqualTo(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
        Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"<>?)", queryString);
    }

    @Test
    public void sqlIsNotIn() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.property(propertyId)).isNotIn(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
        Assert.assertEquals("Wrong query string", "(\"" + columnId + "\" NOT IN (?))", queryString);
    }

    @Test
    public void sqlIsNotInIS() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.property(propertyName1)).isNotIn(qb.parameterValue(paramName1), Boolean.FALSE);
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") NOT IN (LOWER(?)))", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsNotInArray() throws Exception {
        var values = new String[] { "value1", "value2", "value3" };

        var rootOperand = qb.let(qb.column(columnName1)).isNotIn(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, values, rootOperand, parameters);
        Assert.assertEquals(values.length, parameters.size());
        for (int a = values.length; a-- > 0; ) {
            Assert.assertEquals(values[a], parameters.get(a));
        }
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsNotInList() throws Exception {
        var values = new ArrayList<>(new String[] { "value1", "value2", "value3" });

        var rootOperand = qb.let(qb.column(columnName1)).isNotIn(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, values, rootOperand, parameters);
        Assert.assertEquals(values.size(), parameters.size());
        for (int a = values.size(); a-- > 0; ) {
            Assert.assertEquals(values.get(a), parameters.get(a));
        }
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlIsNotInSet() throws Exception {
        var listValues = new ArrayList<>(new String[] { "value1", "value2", "value3" });
        var values = new LinkedHashSet<>(listValues);

        var rootOperand = qb.let(qb.column(columnName1)).isNotIn(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, values, rootOperand, parameters);
        Assert.assertEquals(listValues.size(), parameters.size());
        for (int a = listValues.size(); a-- > 0; ) {
            Assert.assertEquals(listValues.get(a), parameters.get(a));
        }
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlLike() throws Exception {
        var value1 = "test%Value%";

        var rootOperand = qb.let(qb.column(columnName1)).like(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlNotLike() throws Exception {
        var value1 = "test%Value%";

        var rootOperand = qb.let(qb.column(columnName1)).notLike(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')", queryString);
    }

    @Test
    public void sqlRegexpLike() throws Exception {
        var value1 = "testValue1";
        var value2 = "testValue2";

        var rootOperand = qb.regexpLike(qb.property("Name1"), qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "REGEXP_LIKE(\"" + columnName1 + "\",?)", queryString);

        parameters = new ArrayList<>();
        rootOperand = qb.regexpLike(qb.property("Name1"), qb.parameterValue(paramName1), qb.parameterValue(paramName2));
        queryString = buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals(value2, parameters.get(1));
        Assert.assertEquals("Wrong query string", "REGEXP_LIKE(\"" + columnName1 + "\",?,?)", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void sqlNotContains() throws Exception {
        var value1 = "testValue";

        var rootOperand = qb.let(qb.column(columnName1)).notContains(qb.parameterValue(paramName1));
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals("%" + value1 + "%", parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')", queryString);
    }

    @Test
    public void sqlOr() throws Exception {
        Integer value1 = 55;
        Integer value2 = 77;

        var rootOperand = qb.or(qb.let(qb.property(propertyId)).isEqualTo(qb.parameterValue(paramName1)), qb.let(qb.property(propertyVersion)).isEqualTo(qb.parameterValue(paramName2)));
        var queryString = buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
        Assert.assertEquals(value2, conversionHelper.convertValueToType(Integer.class, parameters.get(1)));
        Assert.assertEquals("Wrong query string", "((\"" + columnId + "\"=?) OR (\"" + columnVersion + "\"=?))", queryString);
    }

    @Test
    public void sqlAnd() throws Exception {
        Integer value1 = 55;
        Integer value2 = 77;

        var rootOperand = qb.and(qb.let(qb.property(propertyId)).isEqualTo(qb.parameterValue(paramName1)), qb.let(qb.property(propertyVersion)).isEqualTo(qb.parameterValue(paramName2)));
        var queryString = buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, parameters);
        Assert.assertEquals(value1, conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
        Assert.assertEquals(value2, conversionHelper.convertValueToType(Integer.class, parameters.get(1)));
        Assert.assertEquals("Wrong query string", "((\"" + columnId + "\"=?) AND (\"" + columnVersion + "\"=?))", queryString);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Ignore
    public void sqlOrderBy() throws Exception {
        Integer value1 = 55;

        var rootOperand = qb.let(qb.column(columnName1)).isEqualTo(qb.parameterValue(paramName1));

        qb.orderBy(qb.column(columnName1), OrderByType.DESC);
        var queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
        Assert.assertEquals(value1, parameters.get(0));
        Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?) ORDER BY \"" + columnName1 + "\" DESC", queryString);
    }
}
