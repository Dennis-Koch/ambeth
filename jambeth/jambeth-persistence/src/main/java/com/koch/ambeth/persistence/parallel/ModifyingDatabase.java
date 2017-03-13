package com.koch.ambeth.persistence.parallel;

import java.sql.Connection;
import java.sql.SQLException;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
