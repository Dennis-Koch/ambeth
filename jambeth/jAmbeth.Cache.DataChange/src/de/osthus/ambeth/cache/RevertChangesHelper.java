package de.osthus.ambeth.cache;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.IRevertChangesHelper;
import de.osthus.ambeth.merge.IRevertChangesSavepoint;
import de.osthus.ambeth.merge.RevertChangesFinishedCallback;

public class RevertChangesHelper implements IRevertChangesHelper, IInitializingBean
{
	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public IRevertChangesSavepoint createSavepoint(Object source)
	{
		// Not yet implemented. Ignoring operation intentionally
		return null;
	}

	@Override
	public void revertChanges(Object objectsToRevert)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChanges(Object objectsToRevert, boolean recursive)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, boolean recursive)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
	{
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive)
	{
		// Not yet implemented. Ignoring operation intentionally
	}
}