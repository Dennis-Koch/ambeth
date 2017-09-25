package com.koch.ambeth.query.backwards;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/query/backwards/BackwardsQuery_orm.xml")
@SQLStructure("BackwardsQuery_structure.sql")
@SQLData("BackwardsQuery_data.sql")
public class BackwardsQueryTest extends AbstractInformationBusWithPersistenceTest {
	protected IQueryBuilder<QueryEntity> qb;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		qb = queryBuilderFactory.create(QueryEntity.class);
	}

	@Test
	public void testSimpleBackwardsPropertyQuery() throws Exception {
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "QUERY_ENTITY" J_A ON (S_A."ID"=J_A."NEXT")
		// WHERE (J_A."ID"=?)
		IOperand rootOperand = qb.let(qb.property("<Next")).isEqualTo(qb.value(1));
		IQuery<QueryEntity> query = qb.build(rootOperand);
		assertNotNull(query);
		QueryEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(2, entity.getId());
	}

	@Test
	public void testLongerBackwardsPropertyQuery() throws Exception {
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "QUERY_ENTITY" J_A ON (S_A."NEXT"=J_A."ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."JOIN_QUERY_ENTITY"=J_B."ID")
		// WHERE (J_B."VALUE_FIELD"=?)
		IOperand rootOperand = qb.let(qb.property("Next <QueryEntity.Value")).isEqualTo(qb.value(77));
		IQuery<QueryEntity> query = qb.build(rootOperand);
		assertNotNull(query);
		QueryEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(1, entity.getId());
	}

	@Test
	public void testLinkTableBackwardsPropertyQuery() throws Exception {
		// SELECT DISTINCT S_A."ID",S_A."VERSION",S_A."NAME" FROM "QUERY_ENTITY" S_A
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_A ON (S_A."JOIN_QUERY_ENTITY"=J_A."ID")
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_B ON (J_A."ID"=J_B."LEFT_ID")
		// LEFT OUTER JOIN "LINK_TABLE_ENTITY" J_C ON (J_B."RIGHT_ID"=J_C."ID")
		// WHERE (J_C."NAME"=?)
		IOperand rootOperand =
				qb.let(qb.property("<QueryEntity <JoinQueryEntity Name")).isEqualTo(qb.value("name21"));
		IQuery<QueryEntity> query = qb.build(rootOperand);
		assertNotNull(query);
		QueryEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(2, entity.getId());
	}

	@Test
	public void testNotUniqueBackwardsPropertyQuery() throws Exception {
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "LINK_TABLE_ENTITY" S_A
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_A ON (S_A."ID"=J_A."RIGHT_ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."LEFT_ID"=J_B."ID")
		// WHERE (J_B."VERSION"=?)

		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperand rootOperand =
				qb.let(qb.property("<JoinQueryEntity#LinkTableEntity Version")).isEqualTo(qb.value(3));
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);

		LinkTableEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(21, entity.getId());
	}

	@Test
	public void testNotUniqueBackwardsPropertyQuery_Fullname() throws Exception {
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "LINK_TABLE_ENTITY" S_A
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_A ON (S_A."ID"=J_A."RIGHT_ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."LEFT_ID"=J_B."ID")
		// WHERE (J_B."VERSION"=?)

		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperand rootOperand = qb.let(
				qb.property("<com.koch.ambeth.query.backwards.JoinQueryEntity#LinkTableEntity.Version"))
				.isEqualTo(
						qb.value(3));
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);

		LinkTableEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(21, entity.getId());
	}

	@Test(expected = IllegalStateException.class)
	public void testNotUniqueBackwardsPropertyQuery_missingEntity() throws Exception {
		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperand rootOperand = qb.let(qb.property("<LinkTableEntity.Version")).isEqualTo(qb.value(1));
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);
	}

	@Test
	public void testTwoBackwardsPropertiesQuery() throws Exception {
		// SELECT DISTINCT S_A."ID",S_A."VERSION" FROM "LINK_TABLE_ENTITY" S_A
		// LEFT OUTER JOIN "LINK_JQE_LTE" J_A ON (S_A."ID"=J_A."RIGHT_ID")
		// LEFT OUTER JOIN "JOIN_QUERY_ENTITY" J_B ON (J_A."LEFT_ID"=J_B."ID")
		// LEFT OUTER JOIN "QUERY_ENTITY" J_C ON (S_A."ID"=J_C."LINK_TABLE_ENTITY")
		// WHERE ((J_B."VERSION"=?) AND (J_C."NAME"=?))

		IQueryBuilder<LinkTableEntity> qb = queryBuilderFactory.create(LinkTableEntity.class);

		IOperator operator1 =
				qb.let(qb.property("<JoinQueryEntity#LinkTableEntity Version")).isEqualTo(qb.value(3));
		IOperator operator2 =
				qb.let(qb.property("<QueryEntity#LinkTableEntity Name")).isEqualTo(qb.value("name1"));
		IOperand rootOperand = qb.and(operator1, operator2);
		IQuery<LinkTableEntity> query = qb.build(rootOperand);
		assertNotNull(query);

		LinkTableEntity entity = query.retrieveSingle();
		assertNotNull(entity);
		assertEquals(21, entity.getId());
	}
}
