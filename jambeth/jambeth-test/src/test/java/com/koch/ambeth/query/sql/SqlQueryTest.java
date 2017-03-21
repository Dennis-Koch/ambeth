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

import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.QueryEntity;
import com.koch.ambeth.query.jdbc.StringQuery;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashSet;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/query/Query_orm.xml")
@SQLStructure("../Query_structure.sql")
@SQLData("../Query_data.sql")
public class SqlQueryTest extends AbstractInformationBusWithPersistenceTest {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IQueryBuilder<?> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<>();

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
		nameToValueMap.clear();
	}

	protected String buildQuery(IOperand rootOperand) {
		StringQuery query = beanContext.registerBean(StringQuery.class)//
				.propertyValue("EntityType", Object.class)//
				.propertyValue("RootOperand", rootOperand)//
				.finish();

		ArrayList<Object> parameters = new ArrayList<>();

		return query.fillQuery(nameToValueMap, parameters);
	}

	protected String buildSimpleQuery(String paramName, Object value, IOperand rootOperand,
			IList<Object> parameters) {
		StringQuery query = beanContext.registerBean(StringQuery.class)//
				.propertyValue("EntityType", Object.class)//
				.propertyValue("RootOperand", rootOperand)//
				.finish();

		nameToValueMap.put(paramName, value);

		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		return query.fillQuery(nameToValueMap, parameters);
	}

	protected String buildCompositeQuery(String paramName1, Object value1, String paramName2,
			Object value2, IOperand rootOperand, IList<Object> parameters) {
		StringQuery query = beanContext.registerBean(StringQuery.class)//
				.propertyValue("EntityType", Object.class)//
				.propertyValue("RootOperand", rootOperand)//
				.finish();

		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		nameToValueMap.put(paramName1, value1);
		nameToValueMap.put(paramName2, value2);
		return query.fillQuery(nameToValueMap, parameters);
	}

	@Test
	public void testDirectValue() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isEqualTo(qb.property(propertyName1), qb.value(value1));
		String queryString = buildQuery(rootOperand);
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@Test
	public void simpleSimpleValue() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isEqualTo(qb.property(propertyId), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
		Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"=?)", queryString);
	}

	@Test
	public void escapeString() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.isEqualTo(qb.property(propertyName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlStartsWith() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.startsWith(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlContains() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.contains(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')",
				queryString);
	}

	@Test
	public void sqlCount() throws Exception {
		IQuery<?> query = qb.build();
		Assert.assertEquals(6, query.count());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlEndsWith() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.endsWith(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlGroupBy() throws Exception {
		qb.groupBy(qb.column(columnName1));

		IQuery<?> query = qb.build(qb.all());
		IQueryKey queryKey = query.getQueryKey(null);
		Assert.assertEquals(QueryEntity.class.getName() + "###GROUP BY \"" + columnName1 + "\"#",
				queryKey.toString());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsContainedIn() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.isContainedIn(qb.valueName(paramName1), qb.column(columnName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsContainedInIS() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand =
				qb.isContainedIn(qb.valueName(paramName1), qb.column(columnName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string",
				"(LOWER(\"" + columnName1 + "\") LIKE LOWER(?) ESCAPE '\\')", queryString);
	}

	@Test
	public void sqlIsEmpty() throws Exception {
		IQuery<?> query = qb.build();
		Assert.assertFalse(query.isEmpty());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsEqualTo() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@Test
	public void sqlIsGreaterThan() throws Exception {
		Object value1 = new Double(55);

		IOperand rootOperand = qb.isGreaterThan(qb.property(propertyContent), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Double.class, parameters.get(0)));
		Assert.assertEquals("Wrong query string", "(\"" + columnContent + "\">?)", queryString);
	}

	@Test
	public void sqlIsGreaterThanOrEqualTo() throws Exception {
		Object value1 = new Double(55);

		IOperand rootOperand =
				qb.isGreaterThanOrEqualTo(qb.property(propertyContent), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnContent + "\">=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsIn() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.isIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(1, parameters.size());
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" IN (?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsInIS() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.isIn(qb.column(columnName1), qb.valueName(paramName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(1, parameters.size());
		Assert.assertEquals(value1.toString(), parameters.get(0));
		Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") IN (LOWER(?)))",
				queryString);
	}

	@Test
	public void sqlIsLessThan() throws Exception {
		Object value1 = new Short((short) 55);

		IOperand rootOperand = qb.isLessThan(qb.property(propertyId), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Short.class, parameters.get(0)));
		Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"<?)", queryString);
	}

	@Test
	public void sqlIsLessThanOrEqualTo() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand =
				qb.isLessThanOrEqualTo(qb.property(propertyId), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
		Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"<=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotContainedIn() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.isNotContainedIn(qb.valueName(paramName1), qb.column(columnName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotContainedInIS() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand =
				qb.isNotContainedIn(qb.valueName(paramName1), qb.column(columnName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string",
				"(LOWER(\"" + columnName1 + "\") NOT LIKE LOWER(?) ESCAPE '\\')", queryString);
	}

	@Test
	public void sqlIsNotEqualTo() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isNotEqualTo(qb.property(propertyId), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
		Assert.assertEquals("Wrong query string", "(\"" + columnId + "\"<>?)", queryString);
	}

	@Test
	public void sqlIsNotIn() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isNotIn(qb.property(propertyId), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(1, parameters.size());
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
		Assert.assertEquals("Wrong query string", "(\"" + columnId + "\" NOT IN (?))", queryString);
	}

	@Test
	public void sqlIsNotInIS() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand =
				qb.isNotIn(qb.property(propertyName1), qb.valueName(paramName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(1, parameters.size());
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") NOT IN (LOWER(?)))",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInArray() throws Exception {
		String[] values = new String[] {"value1", "value2", "value3"};

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, values, rootOperand, parameters);
		Assert.assertEquals(values.length, parameters.size());
		for (int a = values.length; a-- > 0;) {
			Assert.assertEquals(values[a], parameters.get(a));
		}
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInList() throws Exception {
		List<String> values = new ArrayList<>(new String[] {"value1", "value2", "value3"});

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, values, rootOperand, parameters);
		Assert.assertEquals(values.size(), parameters.size());
		for (int a = values.size(); a-- > 0;) {
			Assert.assertEquals(values.get(a), parameters.get(a));
		}
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInSet() throws Exception {
		ArrayList<String> listValues =
				new ArrayList<>(new String[] {"value1", "value2", "value3"});
		Set<String> values = new LinkedHashSet<>();
		values.addAll(listValues);

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, values, rootOperand, parameters);
		Assert.assertEquals(listValues.size(), parameters.size());
		for (int a = listValues.size(); a-- > 0;) {
			Assert.assertEquals(listValues.get(a), parameters.get(a));
		}
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlLike() throws Exception {
		Object value1 = "test%Value%";

		IOperand rootOperand = qb.like(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlNotLike() throws Exception {
		Object value1 = "test%Value%";

		IOperand rootOperand = qb.notLike(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')",
				queryString);
	}

	@Test
	public void sqlRegexpLike() throws Exception {
		Object value1 = "testValue1";
		Object value2 = "testValue2";

		IOperand rootOperand = qb.regexpLike(qb.property("Name1"), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string", "REGEXP_LIKE(\"" + columnName1 + "\",?)",
				queryString);

		parameters = new ArrayList<>();
		rootOperand =
				qb.regexpLike(qb.property("Name1"), qb.valueName(paramName1), qb.valueName(paramName2));
		queryString =
				buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals(value2, parameters.get(1));
		Assert.assertEquals("Wrong query string", "REGEXP_LIKE(\"" + columnName1 + "\",?,?)",
				queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlNotContains() throws Exception {
		Object value1 = "testValue";

		IOperand rootOperand = qb.notContains(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals("%" + value1 + "%", parameters.get(0));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')",
				queryString);
	}

	@Test
	public void sqlOr() throws Exception {
		Object value1 = new Integer(55);
		Object value2 = new Integer(77);

		IOperand rootOperand = qb.or(qb.isEqualTo(qb.property(propertyId), qb.valueName(paramName1)),
				qb.isEqualTo(qb.property(propertyVersion), qb.valueName(paramName2)));
		String queryString =
				buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
		Assert.assertEquals(value2,
				conversionHelper.convertValueToType(Integer.class, parameters.get(1)));
		Assert.assertEquals("Wrong query string",
				"((\"" + columnId + "\"=?) OR (\"" + columnVersion + "\"=?))", queryString);
	}

	@Test
	public void sqlAnd() throws Exception {
		Object value1 = new Integer(55);
		Object value2 = new Integer(77);

		IOperand rootOperand = qb.and(qb.isEqualTo(qb.property(propertyId), qb.valueName(paramName1)),
				qb.isEqualTo(qb.property(propertyVersion), qb.valueName(paramName2)));
		String queryString =
				buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, parameters);
		Assert.assertEquals(value1,
				conversionHelper.convertValueToType(Integer.class, parameters.get(0)));
		Assert.assertEquals(value2,
				conversionHelper.convertValueToType(Integer.class, parameters.get(1)));
		Assert.assertEquals("Wrong query string",
				"((\"" + columnId + "\"=?) AND (\"" + columnVersion + "\"=?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	@Ignore
	public void sqlOrderBy() throws Exception {
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));

		qb.orderBy(qb.column(columnName1), OrderByType.DESC);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, parameters);
		Assert.assertEquals(value1, parameters.get(0));
		Assert.assertEquals("Wrong query string",
				"(\"" + columnName1 + "\"=?) ORDER BY \"" + columnName1 + "\" DESC", queryString);
	}
}
