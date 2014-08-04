package de.osthus.ambeth.testutil.persistencerunner;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.AmbethPersistenceRunner;

/**
 * AmbethPersistenceRunner which counts the number of some method calls. Only for testing reasons!
 */
public class CountingPersistenceRunner extends AmbethPersistenceRunner
{

	private int rebuildDataCount = 0;
	private int rebuildStructureCount = 0;
	private int rebuildContextCount = 0;
	private int truncateAllTablesCount = 0;

	public CountingPersistenceRunner(final Class<?> testClass) throws InitializationError
	{
		super(testClass);
	}

	/**
	 * @return the number of calls to method rebuildData
	 */
	public int getRebuildDataCount()
	{
		return rebuildDataCount;
	}

	/**
	 * @return the number of calls to method rebuildStructure
	 */
	public int getRebuildStructureCount()
	{
		return rebuildStructureCount;
	}

	/**
	 * @return the number of calls to method rebuildContext
	 */
	public int getRebuildContextCount()
	{
		return rebuildContextCount;
	}

	/**
	 * @return the number of calls to method truncateAllTables
	 */
	public int getTruncateAllTablesCount()
	{
		return truncateAllTablesCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void rebuildData(final FrameworkMethod frameworkMethod)
	{
		super.rebuildData(frameworkMethod);
		rebuildDataCount++;
	}

	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// protected void rebuildStructure(FrameworkMethod frameworkMethod)
	// {
	// super.rebuildStructure(frameworkMethod);
	// rebuildStructureCount++;
	// }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebuildStructure()
	{
		super.rebuildStructure();
		rebuildStructureCount++;
	}

	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// protected void rebuildContext(FrameworkMethod frameworkMethod)
	// {
	// super.rebuildContext(frameworkMethod);
	// if (frameworkMethod != null)
	// {
	// rebuildContextCount++;
	// }
	// }

	@Override
	protected void rebuildContextDetails(final IBeanContextFactory childContextFactory)
	{
		super.rebuildContextDetails(childContextFactory);
		rebuildContextCount++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void truncateAllTablesBySchema(Connection conn, String... schemaNames) throws SQLException
	{
		super.truncateAllTablesBySchema(conn, schemaNames);
		truncateAllTablesCount++;
	}

}
