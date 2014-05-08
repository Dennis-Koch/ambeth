package de.osthus.ambeth.merge;

public interface IRevertChangesHelper
{
	void revertChanges(Object objectsToRevert);

	void revertChangesGlobally(Object objectsToRevert);
}
