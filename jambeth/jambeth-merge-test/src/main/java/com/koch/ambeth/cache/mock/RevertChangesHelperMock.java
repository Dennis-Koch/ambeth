package com.koch.ambeth.cache.mock;

import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.RevertChangesFinishedCallback;

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