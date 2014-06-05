package de.osthus.ambeth.persistence.schema;import static org.junit.Assert.assertEquals;import static org.junit.Assert.assertNotNull;import static org.junit.Assert.assertNull;import static org.junit.Assert.assertTrue;import java.util.List;import org.junit.Test;import de.osthus.ambeth.cache.ICache;import de.osthus.ambeth.ioc.annotation.Autowired;import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;import de.osthus.ambeth.persistence.schema.models.ChildA;import de.osthus.ambeth.persistence.schema.models.ChildB;import de.osthus.ambeth.persistence.schema.models.IParentAService;import de.osthus.ambeth.persistence.schema.models.IParentBService;import de.osthus.ambeth.persistence.schema.models.ParentA;import de.osthus.ambeth.persistence.schema.models.ParentB;import de.osthus.ambeth.query.IQuery;import de.osthus.ambeth.query.IQueryBuilder;import de.osthus.ambeth.query.IQueryBuilderFactory;import de.osthus.ambeth.service.config.ConfigurationConstants;import de.osthus.ambeth.testutil.AbstractPersistenceTest;import de.osthus.ambeth.testutil.SQLData;import de.osthus.ambeth.testutil.SQLStructure;import de.osthus.ambeth.testutil.TestModule;import de.osthus.ambeth.testutil.TestProperties;import de.osthus.ambeth.testutil.TestPropertiesList;/* * Granting rights to user in other schema: * GRANT ALL ON <other schema name>.<table name> TO <user name>; * GRANT SELECT ON <other schema name>.<sequence name> TO <user name>; */@TestModule({ MultiSchemaTestModule.class })@TestPropertiesList({ @TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName, value = "ci_admin:ci_admin_b"),		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict, value = "true"),		@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/schema/orm.xml") })@SQLStructure("structure.sql")@SQLData("data.sql")public class MultiSchemaTest extends AbstractPersistenceTest{	@Autowired	private ICache cache;	@Autowired	private IQueryBuilderFactory qbf;	@Autowired	private IParentAService parentAService;	@Autowired	private IParentBService parentBService;	@Test	public void testCreateA() throws Exception	{		ChildA child = entityFactory.createEntity(ChildA.class);		ParentA parent = entityFactory.createEntity(ParentA.class);		parent.setChild(child);		parentAService.create(parent);	}	@Test	public void testCreateB() throws Exception	{		ChildB child = entityFactory.createEntity(ChildB.class);		ParentB parent = entityFactory.createEntity(ParentB.class);		parent.setChild(child);		parentBService.create(parent);	}	@Test	public void testRetrieveA() throws Exception	{		ParentA parent = parentAService.retrieve(1);		assertNotNull(parent);		assertEquals(1, parent.getId());		assertEquals(1, parent.getVersion());		ChildA child = parent.getChild();		assertNotNull(child);		assertEquals(11, child.getId());		assertEquals(1, child.getVersion());	}	@Test	public void testRetrieveB() throws Exception	{		ParentB parent = parentBService.retrieve(101);		assertNotNull(parent);		assertEquals(101, parent.getId());		assertEquals(1, parent.getVersion());		ChildB child = parent.getChild();		assertNotNull(child);		assertEquals(111, child.getId());		assertEquals(1, child.getVersion());	}	@Test	public void testUpdateA() throws Exception	{		ParentA parent = cache.getObject(ParentA.class, 1);		ChildA child = parent.getChild();		parent.setChild(entityFactory.createEntity(ChildA.class));		parentAService.update(parent);		ParentA actual = cache.getObject(ParentA.class, 1);		assertEquals(2, actual.getVersion());		assertTrue(child.getId() != actual.getChild().getId());	}	@Test	public void testUpdateB() throws Exception	{		ParentB parent = cache.getObject(ParentB.class, 101);		ChildB child = parent.getChild();		parent.setChild(entityFactory.createEntity(ChildB.class));		parentBService.update(parent);		ParentB actual = cache.getObject(ParentB.class, 101);		assertEquals(2, actual.getVersion());		assertTrue(child.getId() != actual.getChild().getId());	}	@Test	public void testDeleteA() throws Exception	{		ParentA parent = cache.getObject(ParentA.class, 1);		assertNotNull(parent);		parentAService.delete(parent);		ParentA actual = cache.getObject(ParentA.class, 1);		assertNull(actual);	}	@Test	public void testDeleteB() throws Exception	{		ParentB parent = cache.getObject(ParentB.class, 101);		assertNotNull(parent);		parentBService.delete(parent);		ParentB actual = cache.getObject(ParentB.class, 101);		assertNull(actual);	}	@Test	public void testQueryA() throws Exception	{		IQueryBuilder<ParentA> qb = qbf.create(ParentA.class);		IQuery<ParentA> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(11)));		List<ParentA> result = query.retrieve();		assertEquals(1, result.size());		ParentA actual = result.get(0);		assertEquals(1, actual.getId());		assertEquals(11, actual.getChild().getId());	}	@Test	public void testQueryB() throws Exception	{		IQueryBuilder<ParentB> qb = qbf.create(ParentB.class);		IQuery<ParentB> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(111)));		List<ParentB> result = query.retrieve();		assertEquals(1, result.size());		ParentB actual = result.get(0);		assertEquals(101, actual.getId());		assertEquals(111, actual.getChild().getId());	}}