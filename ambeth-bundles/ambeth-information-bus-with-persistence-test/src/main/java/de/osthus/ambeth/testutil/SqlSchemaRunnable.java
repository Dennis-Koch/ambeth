package de.osthus.ambeth.testutil;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlSchemaRunnable implements ISchemaRunnable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public SqlSchemaRunnable()
	{
	}
}
