package de.osthus.ambeth.sql;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.objectcollector.NoOpObjectCollector;
import de.osthus.ambeth.persistence.PersistenceHelper;
import de.osthus.ambeth.util.ReflectUtil;

public class SqlBuilderTest
{
	private static final int batchSize = 3;

	private SqlBuilder fixture;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
		NoOpObjectCollector oc = new NoOpObjectCollector();
		this.fixture = new SqlBuilder();

		PersistenceHelper persistenceHelper = new PersistenceHelper();
		ReflectUtil.getDeclaredField(persistenceHelper.getClass(), "batchSize").set(persistenceHelper, batchSize);
		ReflectUtil.getDeclaredField(persistenceHelper.getClass(), "objectCollector").set(persistenceHelper, oc);
		this.fixture.setObjectCollector(oc);
		this.fixture.setPersistenceHelper(persistenceHelper);

		this.fixture.afterPropertiesSet();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testAfterPropertiesSet()
	{
		this.fixture.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_noPersistenceHelper()
	{
		this.fixture.persistenceHelper = null;
		this.fixture.afterPropertiesSet();
	}

	@Test
	public void testSetPersistenceHelper()
	{
		assertNotNull(this.fixture.persistenceHelper);
		this.fixture.setPersistenceHelper(null);
		assertNull(this.fixture.persistenceHelper);
	}

	@Test
	@Ignore
	public void testAppendNameValue()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testAppendNameValues()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testAppendName()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testAppendValue()
	{
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public void testIsUnescapedType()
	{
		fail("Not yet implemented"); // TODO
	}
}
