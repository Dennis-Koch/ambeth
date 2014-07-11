package de.osthus.ambeth.randomuser;import static org.junit.Assert.assertEquals;import static org.junit.Assert.assertNotNull;import static org.junit.Assert.assertNull;import java.util.List;import org.junit.AfterClass;import org.junit.BeforeClass;import org.junit.Ignore;import org.junit.Test;import org.junit.runner.RunWith;import de.osthus.ambeth.cache.ICache;import de.osthus.ambeth.config.ServiceConfigurationConstants;import de.osthus.ambeth.exception.RuntimeExceptionUtil;import de.osthus.ambeth.ioc.annotation.Autowired;import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;import de.osthus.ambeth.query.IQuery;import de.osthus.ambeth.query.IQueryBuilder;import de.osthus.ambeth.query.IQueryBuilderFactory;import de.osthus.ambeth.randomuser.models.IParentAService;import de.osthus.ambeth.randomuser.models.IParentBService;import de.osthus.ambeth.randomuser.models.ParentA;import de.osthus.ambeth.testutil.AbstractPersistenceTest;import de.osthus.ambeth.testutil.NewAmbethPersistenceRunner;import de.osthus.ambeth.testutil.RandomUserScript;import de.osthus.ambeth.testutil.SQLData;import de.osthus.ambeth.testutil.SQLStructure;import de.osthus.ambeth.testutil.TestModule;import de.osthus.ambeth.testutil.TestProperties;import de.osthus.ambeth.testutil.TestPropertiesList;/** * Tests the replacement of the JAMBETH user by a temporary user created by the RandomUserScript. Because this is also done by the Jenkins build script this * test won't run in the Jenkins environment. Therefore the test methods are disabled. */@TestModule({ MultiSchemaTestModule.class })@TestPropertiesList({ @TestProperties(file = "single_random_user_test.properties"),		@TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict, value = "true"),		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/schema/single_random_user_orm.xml") })@SQLStructure("single_random_user_structure.sql")@SQLData("single_random_user_data.sql")@RunWith(NewAmbethPersistenceRunner.class)@Ignore("Does not work in JENKINS")public class SingleRandomUserTest extends AbstractPersistenceTest{	private static final String RANDOM_USER_TEST_PROPERTIES = "single_random_user_test.properties";	@Autowired	private ICache cache;	@Autowired	private IQueryBuilderFactory qbf;	@Autowired	private IParentAService parentAService;	@Autowired	private IParentBService parentBService;	@BeforeClass	public static void beforeClass()	{		String[] args = new String[] { RandomUserScript.SCRIPT_IS_CREATE + "=true", RandomUserScript.SCRIPT_USER_PASS + "=pw",				RandomUserScript.SCRIPT_USER_PROPERTYFILE + "=" + RANDOM_USER_TEST_PROPERTIES };		runRandomUserScript(args);	}	@AfterClass	public static void afterClass()	{		String[] args = new String[] { RandomUserScript.SCRIPT_IS_CREATE + "=false",				RandomUserScript.SCRIPT_USER_PROPERTYFILE + "=" + RANDOM_USER_TEST_PROPERTIES };		runRandomUserScript(args);	}	private static void runRandomUserScript(String[] args)	{		try		{			RandomUserScript.main(args);		}		catch (Throwable e)		{			throw RuntimeExceptionUtil.mask(e);		}	}	@Test	public void testDelete() throws Exception	{		ParentA parent = cache.getObject(ParentA.class, 1);		assertNotNull(parent);		parentAService.delete(parent);		ParentA actual = cache.getObject(ParentA.class, 1);		assertNull(actual);	}	@Test	public void testQuery() throws Exception	{		IQueryBuilder<ParentA> qb = qbf.create(ParentA.class);		IQuery<ParentA> query = qb.build(qb.isEqualTo(qb.property("Child.Id"), qb.value(11)));		List<ParentA> result = query.retrieve();		assertEquals(1, result.size());		ParentA actual = result.get(0);		assertEquals(1, actual.getId());		assertEquals(11, actual.getChild().getId());	}}