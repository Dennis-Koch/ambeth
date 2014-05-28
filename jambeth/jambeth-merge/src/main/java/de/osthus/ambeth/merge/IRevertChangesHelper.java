package de.osthus.ambeth.merge;

public interface IRevertChangesHelper
{
	IRevertChangesSavepoint createSavepoint(Object source);

	void revertChanges(Object objectsToRevert);

	void revertChanges(Object objectsToRevert, boolean recursive);

	void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback);

	void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive);

	void revertChangesGlobally(Object objectsToRevert);

	void revertChangesGlobally(Object objectsToRevert, boolean recursive);

	void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback);

	void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive);
}