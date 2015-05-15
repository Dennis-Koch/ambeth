package de.osthus.ambeth.persistence.parallel;

import java.sql.Connection;
import java.sql.SQLException;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ModifyingDatabase implements IModifyingDatabase
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected Connection connection;

	protected boolean modifyingDatabase;

	protected boolean modifyingAllowed = true;

	@Override
	public boolean isModifyingAllowed()
	{
		return modifyingAllowed;
	}

	@Override
	public void setModifyingAllowed(boolean modifyingAllowed)
	{
		this.modifyingAllowed = modifyingAllowed;
		if (connection != null)
		{
			try
			{
				if (!(connection.isReadOnly() ^ modifyingAllowed))
				{
					connection.setReadOnly(!modifyingAllowed);
				}
			}
			catch (SQLException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public boolean isModifyingDatabase()
	{
		return modifyingDatabase;
	}

	@Override
	public void setModifyingDatabase(boolean modifyingDatabase)
	{
		this.modifyingDatabase = modifyingDatabase;
	}
}
