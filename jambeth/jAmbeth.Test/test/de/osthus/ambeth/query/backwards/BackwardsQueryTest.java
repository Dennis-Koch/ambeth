package de.osthus.ambeth.query.backwards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/backwards/BackwardsQuery_orm.xml")
@SQLStructure("BackwardsQuery_structure.sql")
@SQLData("BackwardsQuery_data.sql")
public class BackwardsQueryTest extends AbstractPersistenceTest
{
	protected IQueryBuilder<QueryEntity> qb;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@Test
	public void testSimpleBackwardsPropertyQuery() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "QUERY_ENTITY" J_A ON (S_A."ID"=J_A."NEXT")
		// WHERE (J_A."ID"=?)
		IOperand rootOperand = qb.isEqualTo(qb.property("<Next"), qb.value(1));
		IQuery<QueryEntity> query = qb.build(rootOperand);
		assertNotNull(query);
		QueryEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(2, entity.getId());
	}

	@Test
	public void testLongerBackwardsPropertyQuery() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "QUERY_ENTITY" J_A ON (S_A."NEXT"=J_A."ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."JOIN_QUERY_ENTITY"=J_B."ID")
		// WHERE (J_B."VALUE_FIELD"=?)
		IOperand rootOperand = qb.isEqualTo(qb.property("Next <QueryEntity.Value"), qb.value(77));
		IQuery<QueryEntity> query = qb.build(rootOperand);
		assertNotNull(query);
		QueryEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(1, entity.getId());
	}

	@Test
	public void testLinkTableBackwardsPropertyQuery() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_A ON (S_A."JOIN_QUERY_ENTITY"=J_A."ID")
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_B ON (J_A."ID"=J_B."LEFT_ID")
		// LEFT OUTER JOIN "LINK_TABLE_ENTITY" J_C ON (J_B."RIGHT_ID"=J_C."ID")
		// WHERE (J_C."NAME"=?)
		IOperand rootOperand = qb.isEqualTo(qb.property("<QueryEntity <JoinQueryEntity Name"), qb.value("name21"));
		IQuery<QueryEntity> query = qb.build(rootOperand);
		assertNotNull(query);
		QueryEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(2, entity.getId());
	}

	@Test
	public void testNotUniqueBackwardsPropertyQuery() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "LINK_TABLE_ENTITY" S_A
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_A ON (S_A."ID"=J_A."RIGHT_ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."LEFT_ID"=J_B."ID")
		// WHERE (J_B."VERSION"=?)

		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperand rootOperand = qb.isEqualTo(qb.property("<JoinQueryEntity#LinkTableEntity Version"), qb.value(3));
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);

		LinkTableEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(21, entity.getId());
	}

	@Test
	public void testNotUniqueBackwardsPropertyQuery_Fullname() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "LINK_TABLE_ENTITY" S_A
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_A ON (S_A."ID"=J_A."RIGHT_ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."LEFT_ID"=J_B."ID")
		// WHERE (J_B."VERSION"=?)

		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperand rootOperand = qb.isEqualTo(qb.property("<de.osthus.ambeth.query.backwards.JoinQueryEntity#LinkTableEntity.Version"), qb.value(3));
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);

		LinkTableEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(21, entity.getId());
	}

	@Test(expected = IllegalStateException.class)
	public void testNotUniqueBackwardsPropertyQuery_missingEntity() throws Exception
	{
		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperand rootOperand = qb.isEqualTo(qb.property("<LinkTableEntity.Version"), qb.value(1));
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);
	}

	@Test
	public void testTwoBackwardsPropertiesQuery() throws Exception
	{
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "LINK_TABLE_ENTITY" S_A
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_A ON (S_A."ID"=J_A."RIGHT_ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."LEFT_ID"=J_B."ID")
		// LEFT OUTER JOIN "QUERY_ENTITY" J_C ON (S_A."ID"=J_C."LINK_TABLE_ENTITY")
		// WHERE ((J_B."VERSION"=?) AND (J_C."NAME"=?))

		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperator operator1 = qb.isEqualTo(qb.property("<JoinQueryEntity#LinkTableEntity Version"), qb.value(3));
		IOperator operator2 = qb.isEqualTo(qb.property("<QueryEntity#LinkTableEntity Name"), qb.value("name1"));
		IOperand rootOperand = qb.and(operator1, operator2);
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);

		LinkTableEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(21, entity.getId());
	}
}
