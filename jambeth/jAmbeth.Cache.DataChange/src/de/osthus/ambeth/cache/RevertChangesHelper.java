package de.osthus.ambeth.cache;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.IRevertChangesHelper;

public class RevertChangesHelper implements IRevertChangesHelper, IInitializingBean
{
	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public void revertChanges(Object objectsToRevert)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert)
	{
		// Not yet implemented. Ignoring operation intentionally
	}
}
