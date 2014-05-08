package de.osthus.ambeth.testutil.persistencerunner;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.IRunnerAware;
import de.osthus.ambeth.testutil.model.Project;

/**
 * Base class of the tests for the AmbethPersistenceRunner.
 */
@RunWith(CountingPersistenceRunner.class)
public abstract class AbstractPersistenceRunnerTest extends AbstractPersistenceTest implements IRunnerAware
{

	protected static BlockJUnit4ClassRunner currentRunner;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRunner(final BlockJUnit4ClassRunner runner)
	{
		currentRunner = runner;
	}

	/* package */void assertDataIsExactly(List<Integer> expectedProjectAlternateKeys)
	{
		IQueryBuilder<Project> queryBuilder = queryBuilderFactory.create(Project.class);
		IQuery<Project> query = queryBuilder.build();
		List<Project> results = query.retrieve();
		if (results == null || results.isEmpty())
		{
			Assert.assertTrue("No projects found but expected!", expectedProjectAlternateKeys == null || expectedProjectAlternateKeys.isEmpty());
		}
		else
		{
			List<Integer> projectKeys = new ArrayList<Integer>();
			if (expectedProjectAlternateKeys != null)
			{
				projectKeys.addAll(expectedProjectAlternateKeys);
			}
			List<Integer> notFound = new ArrayList<Integer>();
			for (Project project : results)
			{
				Integer alternateKey = project.getAlternateKey();
				if (projectKeys.contains(alternateKey))
				{
					projectKeys.remove(alternateKey);
				}
				else
				{
					notFound.add(alternateKey);
				}
			}
			Assert.assertTrue("The following expected projects were not found: [alternateKey=" + StringUtils.join(projectKeys, ", ") + "]!",
					projectKeys.isEmpty());
			Assert.assertTrue("The following projects were not expected: [alternateKey=" + StringUtils.join(notFound, ", ") + "]!", notFound.isEmpty());
		}
	}

	/**
	 * Assert that the given expectations are exactly fulfilled (uses assertEquals).
	 * 
	 * @param expectedRebuildContextCount
	 *            Expected number of context rebuilds
	 * @param expectedRebuildStructureCount
	 *            Expected number of structure rebuilds
	 * @param expectedRebuildDataCount
	 *            Expected number of data rebuilds
	 * @param expectedTruncateAllTablesCount
	 *            Expected number of truncates
	 */
	/* package */static void assertAllCountsExactly(int expectedRebuildContextCount, int expectedRebuildStructureCount, int expectedRebuildDataCount,
			int expectedTruncateAllTablesCount)
	{
		Assert.assertNotNull(currentRunner);

		int rebuildContextCount = ((CountingPersistenceRunner) currentRunner).getRebuildContextCount();
		int rebuildStructureCount = ((CountingPersistenceRunner) currentRunner).getRebuildStructureCount();
		int rebuildDataCount = ((CountingPersistenceRunner) currentRunner).getRebuildDataCount();
		int truncateCount = ((CountingPersistenceRunner) currentRunner).getTruncateAllTablesCount();
		// System.out.println("CountingPersistenceRunner recognized: context build " + rebuildContextCount + "x, structure build " + rebuildStructureCount
		// + "x, data rebuild " + rebuildDataCount + "x, truncate done " + truncateCount + "x");

		// Assert.assertEquals("Rebuild context was called more or less than needed!", expectedRebuildContextCount, rebuildContextCount);
		// Assert.assertEquals("Rebuild structure was called more or less than needed!", expectedRebuildStructureCount, rebuildStructureCount);
		// Assert.assertEquals("Rebuild data was called more or less than needed!", expectedRebuildDataCount, rebuildDataCount);
		// Assert.assertEquals("Truncate all tables was called more or less than needed!", expectedTruncateAllTablesCount, truncateCount);

		StringBuilder assertionStringBuilder = new StringBuilder();
		addAssertion(assertionStringBuilder, "Rebuild context", expectedRebuildContextCount, rebuildContextCount);
		addAssertion(assertionStringBuilder, "Rebuild structure", expectedRebuildStructureCount, rebuildStructureCount);
		addAssertion(assertionStringBuilder, "Rebuild data", expectedRebuildDataCount, rebuildDataCount);
		addAssertion(assertionStringBuilder, "Truncate all tables", expectedTruncateAllTablesCount, truncateCount);
		String assertions = assertionStringBuilder.toString();
		Assert.assertTrue(assertions, StringUtils.isEmpty(assertions));
	}

	private static void addAssertion(StringBuilder sb, String type, int expected, int counted)
	{
		if (counted < expected)
		{
			sb.append(type).append(" was called less than expected [").append(counted).append(" < ").append(expected).append("]! ");
		}
		else if (counted > expected)
		{
			sb.append(type).append(" was called more than expected [").append(counted).append(" > ").append(expected).append("]! ");
		}
	}

}
