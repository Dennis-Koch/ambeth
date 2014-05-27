package de.osthus.ambeth.merge;

import de.osthus.ambeth.util.IDisposable;

public interface IRevertChangesSavepoint extends IDisposable
{
	void revertChanges();

	Object[] getSavedBusinessObjects();
}
