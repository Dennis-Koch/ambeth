package de.osthus.ambeth.query.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.query.QueryEntity;
import de.osthus.ambeth.query.StringQuery;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/Query_orm.xml")
@SQLStructure("../Query_structure.sql")
@SQLData("../Query_data.sql")
public class SqlQueryTest extends AbstractPersistenceTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IQueryBuilder<?> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	protected Map<Integer, Object> params = new HashMap<Integer, Object>();

	protected Stack<Class<?>> columnTypeStack = new Stack<Class<?>>();

	protected String paramName1 = "param.1";
	protected String paramName2 = "param.2";

	protected String columnName1 = "NAME1";
	protected String columnName2 = "NAME2";

	@Before
	public void setUp() throws Exception
	{
		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@After
	public void tearDown() throws Exception
	{
		Assert.assertTrue(columnTypeStack.isEmpty());
		qb = null;
		nameToValueMap.clear();
	}

	protected String buildQuery(IOperand rootOperand)
	{
		StringQuery query = beanContext.registerAnonymousBean(StringQuery.class).propertyValue("RootOperand", rootOperand).finish();

		Map<Integer, Object> params = new HashMap<Integer, Object>();

		return query.fillQuery(nameToValueMap, params);
	}

	protected String buildSimpleQuery(String paramName, Object value, IOperand rootOperand, Map<Integer, Object> params)
	{
		StringQuery query = beanContext.registerAnonymousBean(StringQuery.class).propertyValue("RootOperand", rootOperand).finish();

		nameToValueMap.put(paramName, value);

		if (params == null)
		{
			params = new HashMap<Integer, Object>();
		}
		return query.fillQuery(nameToValueMap, params);
	}

	protected String buildCompositeQuery(String paramName1, Object value1, String paramName2, Object value2, IOperand rootOperand, Map<Integer, Object> params)
	{
		StringQuery query = beanContext.registerAnonymousBean(StringQuery.class).propertyValue("RootOperand", rootOperand).finish();

		if (params == null)
		{
			params = new HashMap<Integer, Object>();
		}
		nameToValueMap.put(paramName1, value1);
		nameToValueMap.put(paramName2, value2);
		return query.fillQuery(nameToValueMap, params);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testDirectValue() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.value(value1));
		String queryString = buildQuery(rootOperand);
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void simpleSimpleValue() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void escapeString() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlStartsWith() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.startsWith(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlContains() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.contains(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlEndsWith() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.endsWith(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsContainedIn() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isContainedIn(qb.valueName(paramName1), qb.column(columnName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsContainedInIS() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isContainedIn(qb.valueName(paramName1), qb.column(columnName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") LIKE LOWER(?) ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsEqualTo() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsGreaterThan() throws Exception
	{
		Object value1 = new Double(55);

		IOperand rootOperand = qb.isGreaterThan(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\">?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsGreaterThanOrEqualTo() throws Exception
	{
		Object value1 = new Float(55);

		IOperand rootOperand = qb.isGreaterThanOrEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\">=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsIn() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(1, params.size());
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" IN (?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsInIS() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isIn(qb.column(columnName1), qb.valueName(paramName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(1, params.size());
		Assert.assertEquals(value1.toString(), params.get(1));
		Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") IN (LOWER(?)))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsLessThan() throws Exception
	{
		Object value1 = new Short((short) 55);

		IOperand rootOperand = qb.isLessThan(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"<?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsLessThanOrEqualTo() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isLessThanOrEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"<=?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotContainedIn() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isNotContainedIn(qb.valueName(paramName1), qb.column(columnName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotContainedInIS() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.isNotContainedIn(qb.valueName(paramName1), qb.column(columnName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") NOT LIKE LOWER(?) ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotEqualTo() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isNotEqualTo(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"<>?)", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotIn() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(1, params.size());
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInIS() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1), Boolean.FALSE);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(1, params.size());
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(LOWER(\"" + columnName1 + "\") NOT IN (LOWER(?)))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInArray() throws Exception
	{
		String[] values = new String[] { "value1", "value2", "value3" };

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, values, rootOperand, params);
		Assert.assertEquals(values.length, params.size());
		for (int a = values.length; a-- > 0;)
		{
			Assert.assertEquals(values[a], params.get(1 + a));
		}
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInList() throws Exception
	{
		List<String> values = new ArrayList<String>(new String[] { "value1", "value2", "value3" });

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, values, rootOperand, params);
		Assert.assertEquals(values.size(), params.size());
		for (int a = values.size(); a-- > 0;)
		{
			Assert.assertEquals(values.get(a), params.get(1 + a));
		}
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlIsNotInSet() throws Exception
	{
		ArrayList<String> listValues = new ArrayList<String>(new String[] { "value1", "value2", "value3" });
		Set<String> values = new LinkedHashSet<String>();
		values.addAll(listValues);

		IOperand rootOperand = qb.isNotIn(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, values, rootOperand, params);
		Assert.assertEquals(listValues.size(), params.size());
		for (int a = listValues.size(); a-- > 0;)
		{
			Assert.assertEquals(listValues.get(a), params.get(1 + a));
		}
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT IN (?,?,?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlLike() throws Exception
	{
		Object value1 = "test%Value%";

		IOperand rootOperand = qb.like(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" LIKE ? ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlNotLike() throws Exception
	{
		Object value1 = "test%Value%";

		IOperand rootOperand = qb.notLike(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlNotContains() throws Exception
	{
		Object value1 = "testValue";

		IOperand rootOperand = qb.notContains(qb.column(columnName1), qb.valueName(paramName1));
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals("%" + value1 + "%", params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\" NOT LIKE ? ESCAPE '\\')", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlOr() throws Exception
	{
		Object value1 = new Integer(55);
		Object value2 = new Integer(77);

		IOperand rootOperand = qb.or(qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1)),
				qb.isEqualTo(qb.column(columnName2), qb.valueName(paramName2)));
		String queryString = buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals(value2, params.get(2));
		Assert.assertEquals("Wrong query string", "((\"" + columnName1 + "\"=?) OR (\"" + columnName2 + "\"=?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void sqlAnd() throws Exception
	{
		Object value1 = new Integer(55);
		Object value2 = new Integer(77);

		IOperand rootOperand = qb.and(qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1)),
				qb.isEqualTo(qb.column(columnName2), qb.valueName(paramName2)));
		String queryString = buildCompositeQuery(paramName1, value1, paramName2, value2, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals(value2, params.get(2));
		Assert.assertEquals("Wrong query string", "((\"" + columnName1 + "\"=?) AND (\"" + columnName2 + "\"=?))", queryString);
	}

	@SuppressWarnings("deprecation")
	@Test
	@Ignore
	public void sqlOrderBy() throws Exception
	{
		Object value1 = new Integer(55);

		IOperand rootOperand = qb.isEqualTo(qb.column(columnName1), qb.valueName(paramName1));

		qb.orderBy(qb.column(columnName1), OrderByType.DESC);
		String queryString = buildSimpleQuery(paramName1, value1, rootOperand, params);
		Assert.assertEquals(value1, params.get(1));
		Assert.assertEquals("Wrong query string", "(\"" + columnName1 + "\"=?) ORDER BY \"" + columnName1 + "\" DESC", queryString);
	}
}