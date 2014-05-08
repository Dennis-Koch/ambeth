package de.osthus.ambeth.cache.mock;

import de.osthus.ambeth.merge.IRevertChangesHelper;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class RevertChangesHelperMock implements IRevertChangesHelper
{
	@Override
	public void revertChanges(Object objectsToRevert)
	{
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert)
	{
	}
}
