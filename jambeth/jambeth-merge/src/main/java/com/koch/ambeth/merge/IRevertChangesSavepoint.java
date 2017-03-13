package com.koch.ambeth.merge;

import com.koch.ambeth.util.IDisposable;

public interface IRevertChangesSavepoint extends IDisposable
{
	void revertChanges();

	Object[] getSavedBusinessObjects();
}
