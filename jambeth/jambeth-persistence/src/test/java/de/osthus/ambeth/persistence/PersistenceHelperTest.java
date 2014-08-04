package de.osthus.ambeth.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.objectcollector.NoOpObjectCollector;
import de.osthus.ambeth.sql.SqlBuilder;

public class PersistenceHelperTest
{
	private static final int batchSize = 3;

	private static final int preparedBatchSize = 3;

	private PersistenceHelper fixture;

	@Before
	public void setUp() throws Exception
	{
		this.fixture = new PersistenceHelper();
		this.fixture.batchSize = batchSize;
		this.fixture.preparedBatchSize = preparedBatchSize;

		NoOpObjectCollector oc = new NoOpObjectCollector();
		this.fixture.objectCollector = oc;

		SqlBuilder sqlBuilder = new SqlBuilder();
		sqlBuilder.setObjectCollector(oc);
		sqlBuilder.setPersistenceHelper(this.fixture);
		sqlBuilder.afterPropertiesSet();
		this.fixture.sqlBuilder = sqlBuilder;
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_wrongBatchSize()
	{
		this.fixture.batchSize = 0;
		this.fixture.afterPropertiesSet();
	}

	@Test
	public void testSetBatchSize()
	{
		int newBatchSize = 0;
		this.fixture.batchSize = newBatchSize;
		assertEquals(newBatchSize, this.fixture.batchSize);
	}

	@Test
	public void testSetObjectCollector()
	{
		assertNotNull(this.fixture.objectCollector);
		this.fixture.objectCollector = null;
		assertNull(this.fixture.objectCollector);
	}

	@Test
	public void testSetSqlBuilder()
	{
		assertNotNull(this.fixture.sqlBuilder);
		this.fixture.sqlBuilder = null;
		assertNull(this.fixture.sqlBuilder);
	}

	@Test
	public void testSplitValues()
	{
		int batchRows = 3;
		List<Object> values = new ArrayList<Object>();
		for (int i = (batchRows - 1) * preparedBatchSize + 1; i-- > 0;)
		{
			values.add(new Object());
		}
		IList<IList<Object>> actual = this.fixture.splitValues(values);
		assertEquals(batchRows, actual.size());
		boolean last = true;
		for (int i = actual.size(); i-- > 0;)
		{
			if (!last)
			{
				assertEquals(batchSize, actual.get(i).size());
			}
			else
			{
				assertEquals(1, actual.get(i).size());
				last = false;
			}
		}
	}

	@Test
	public void testBuildStringListOfValues()
	{
		int batchRows = 4;
		List<Object> values = new ArrayList<Object>();
		for (int i = (batchRows - 1) * batchSize + 1; i-- > 0;)
		{
			values.add(i);
		}
		IList<String> actual = this.fixture.buildStringListOfValues(values);
		assertEquals(batchRows, actual.size());
	}

	@Test
	public void testBuildStringOfValues()
	{
		int count = 4; // should have only one digit
		List<Object> values = new ArrayList<Object>();
		for (int i = count; i-- > 0;)
		{
			values.add(i);
		}
		String actual = this.fixture.buildStringOfValues(values);
		assertEquals(count * 2 - 1, actual.length());
	}

	@Test
	public void testAppendStringOfValues()
	{
		int count = 3; // should have only one digit
		List<Object> values = new ArrayList<Object>();
		for (int i = count; i-- > 0;)
		{
			values.add(new Integer(i).toString());
		}
		String actual = this.fixture.buildStringOfValues(values);
		assertEquals(count * 4 - 1, actual.length());
	}
}
