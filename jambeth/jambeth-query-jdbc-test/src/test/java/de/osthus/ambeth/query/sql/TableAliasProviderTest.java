package de.osthus.ambeth.query.sql;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.testutil.AbstractIocTest;

public class TableAliasProviderTest extends AbstractIocTest
{
	private ITableAliasProvider fixture;

	@Before
	public void setUp() throws Exception
	{
		fixture = beanContext.registerAnonymousBean(TableAliasProvider.class).finish();
	}

	@Test
	public void testGetNextJoinAlias()
	{
		String joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_A", joinAlias);
	}

	@Test
	public void testGetNextSubQueryAlias()
	{
		String joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_A", joinAlias);
	}

	@Test
	public void testMixedAliases()
	{
		String joinAlias;
		joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_A", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_A", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_B", joinAlias);

		joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_B", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_C", joinAlias);

		joinAlias = fixture.getNextSubQueryAlias();
		assertEquals("S_C", joinAlias);
	}

	@Test
	public void testMultiCharAlias()
	{
		// Iterating to the next interesting cases
		for (int i = 0; i < 25; i++)
		{
			fixture.getNextJoinAlias();
		}
		String joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_Z", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AA", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AB", joinAlias);

		// Iterating to the next interesting cases
		for (int i = 0; i < 23; i++)
		{
			fixture.getNextJoinAlias();
		}
		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AZ", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_BA", joinAlias);

		// Iterating to the next interesting cases
		for (int i = 0; i < ((2 * 26 - 1) * 26 - 2); i++)
		{
			fixture.getNextJoinAlias();
		}
		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_AZZ", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_BAA", joinAlias);

		joinAlias = fixture.getNextJoinAlias();
		assertEquals("J_BAB", joinAlias);
	}
}
