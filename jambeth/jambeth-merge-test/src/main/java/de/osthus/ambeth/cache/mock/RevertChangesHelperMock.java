package de.osthus.ambeth.cache.mock;

import de.osthus.ambeth.merge.IRevertChangesHelper;
import de.osthus.ambeth.merge.IRevertChangesSavepoint;
import de.osthus.ambeth.merge.RevertChangesFinishedCallback;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class RevertChangesHelperMock implements IRevertChangesHelper
{
	@Override
	public IRevertChangesSavepoint createSavepoint(Object source)
	{
		return null;
	}

	@Override
	public void revertChanges(Object objectsToRevert)
	{
	}

	@Override
	public void revertChanges(Object objectsToRevert, boolean recursive)
	{
	}

	@Override
	public void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
	{
	}

	@Override
	public void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive)
	{
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert)
	{
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, boolean recursive)
	{
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback)
	{
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive)
	{
	}
}