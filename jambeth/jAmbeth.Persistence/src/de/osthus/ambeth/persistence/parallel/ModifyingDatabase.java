package de.osthus.ambeth.persistence.parallel;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ModifyingDatabase implements IInitializingBean, IModifyingDatabase
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected boolean isModifying;

	protected boolean isModifyingAllowed = true;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public boolean isModifyingAllowed()
	{
		return isModifyingAllowed;
	}

	@Override
	public void setModifyingAllowed(boolean isModifyingAllowed)
	{
		this.isModifyingAllowed = isModifyingAllowed;
	}

	@Override
	public boolean isModifyingDatabase()
	{
		return isModifying;
	}

	@Override
	public void setModifyingDatabase(boolean modifying)
	{
		isModifying = modifying;
	}

}
