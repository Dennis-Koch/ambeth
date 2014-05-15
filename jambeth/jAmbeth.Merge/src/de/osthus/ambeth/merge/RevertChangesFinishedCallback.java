package de.osthus.ambeth.merge;

import de.osthus.ambeth.util.IDelegate;

public interface RevertChangesFinishedCallback extends IDelegate
{
	void invoke(boolean success);
}
