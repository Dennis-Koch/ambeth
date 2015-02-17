package de.osthus.ambeth.query;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("StoredFunction_data.sql")
@SQLStructure("StoredFunction_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/Query_orm.xml")
public class StoredFunctionTest extends AbstractInformationBusWithPersistenceTest
{
	protected static final String paramName1 = "param.1";
	protected static final String paramName2 = "param.2";
	protected static final String paramName3 = "param.3";
	protected static final String propertyName1 = "Id";
	protected static final String propertyName2 = "Version";

	protected IQueryBuilder<QueryEntity> qb;

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@Override
	public void destroy() throws Throwable
	{
		super.destroy();
		nameToValueMap.clear();
	}

	@Test
	public void testFinalize() throws Exception
	{
		int value = 2;

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(qb.property(propertyName1), qb.function("getDoubled", qb.value(value))));

		List<QueryEntity> actual = query.retrieve();

		assertEquals(1, actual.size());
		assertEquals(value * 2, actual.get(0).getId());
	}

	@Test
	public void testMultipleParameters() throws Exception
	{
		int value = 2;

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(
				qb.property(propertyName1),
				qb.function("multiParams", qb.property(propertyName2), qb.value("BwQKSwe1RfgnSdDldsfnskQakDl0Q3CbHr2qwXSQin63x81MBm5ryiaE54ohMFSBTr"),
						qb.value(value))));

		List<QueryEntity> actual = query.retrieve();

		assertEquals(1, actual.size());
		assertEquals(value * 2, actual.get(0).getId());
	}

	@Test
	public void testFunctionFirst() throws Exception
	{
		int value = 2;

		IQuery<QueryEntity> query = qb.build(qb.isEqualTo(
				qb.function("multiParams", qb.property(propertyName2), qb.value("BwQKSwe1RfgnSdDldsfnskQakDl0Q3CbHr2qwXSQin63x81MBm5ryiaE54ohMFSBTr"),
						qb.value(value)), qb.value(4)));

		List<QueryEntity> actual = query.retrieve();

		assertEquals(5, actual.size()); // All entities
	}
}
