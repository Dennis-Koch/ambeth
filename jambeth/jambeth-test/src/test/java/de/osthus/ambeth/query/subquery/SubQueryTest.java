package de.osthus.ambeth.query.subquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.ISubQuery;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/subquery/SubQuery_orm.xml")
@SQLStructure("SubQuery_structure.sql")
@SQLData("SubQuery_data.sql")
public class SubQueryTest extends AbstractPersistenceTest
{
	protected IQueryBuilder<EntityA> qb;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		qb = queryBuilderFactory.create(EntityA.class);
	}

	@Test
	public void testBuildSubQuery() throws Exception
	{
		ISubQuery<EntityA> subQuery = qb.buildSubQuery();
		assertNotNull(subQuery);

		String[] sqlParts = subQuery.getSqlParts(new HashMap<Object, Object>(), new ArrayList<Object>(0), EmptyList.<String> getInstance());
		assertNotNull(sqlParts);
		assertEquals(3, sqlParts.length);
		assertNull(sqlParts[0]);
		assertEquals("1=1", sqlParts[1]);
		assertEquals("", sqlParts[2]);
	}

	@Test
	public void testBuildQueryWithSubQuery() throws Exception
	{
		IQueryBuilder<EntityA> qbSub = queryBuilderFactory.create(EntityA.class);
		IOperand versionMain = qb.property("Version");
		IOperand buidSub = qbSub.property("Buid");
		IOperand versionB = qbSub.property("EntityB.Version");
		ISubQuery<EntityA> subQuery = qbSub.buildSubQuery(qbSub.or(qbSub.isIn(qbSub.property("EntityB.Buid"), qbSub.value("BUID 11")),
				qbSub.isEqualTo(versionB, versionMain)));

		IQuery<EntityA> query = qb.build(qb.isIn(qb.property("Buid"), qb.subQuery(subQuery, buidSub)));
		assertNotNull(query);

		IList<EntityA> entityAs = query.retrieve();
		assertEquals(2, entityAs.size());
	}

	@Test
	public void testSubQueryInFunction() throws Exception
	{
		IQueryBuilder<EntityA> qbSub = queryBuilderFactory.create(EntityA.class);
		IOperand versionMain = qb.property("Version");
		IOperand buidSub = qbSub.property("Buid");
		IOperand versionB = qbSub.property("EntityB.Version");
		ISubQuery<EntityA> subQuery = qbSub.buildSubQuery(qbSub.or(qbSub.isIn(qbSub.property("EntityB.Buid"), qbSub.value("BUID 11")),
				qbSub.isEqualTo(versionB, versionMain)));

		IQuery<EntityA> query = qb.build(qb.function("EXISTS", qb.subQuery(subQuery, buidSub)));
		assertNotNull(query);

		IList<EntityA> entityAs = query.retrieve();
		assertEquals(4, entityAs.size());
	}
}
