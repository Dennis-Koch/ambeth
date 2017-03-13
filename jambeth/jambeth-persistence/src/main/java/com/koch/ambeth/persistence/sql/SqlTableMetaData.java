package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.TableMetaData;
import com.koch.ambeth.util.ParamChecker;

public class SqlTableMetaData extends TableMetaData
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String fullqualifiedEscapedName;

	protected Object initialVersion;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(initialVersion, "initialVersion");
		ParamChecker.assertNotNull(fullqualifiedEscapedName, "fullqualifiedEscapedName");
	}

	public Object getInitialVersion()
	{
		return initialVersion;
	}

	public void setInitialVersion(Object initialVersion)
	{
		this.initialVersion = initialVersion;
	}

	@Override
	public String getFullqualifiedEscapedName()
	{
		return fullqualifiedEscapedName;
	}

	public void setFullqualifiedEscapedName(String fullqualifiedEscapedName)
	{
		this.fullqualifiedEscapedName = fullqualifiedEscapedName;
	}
}
