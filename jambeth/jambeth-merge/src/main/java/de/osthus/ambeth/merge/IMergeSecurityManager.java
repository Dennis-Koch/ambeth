package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.model.IMethodDescription;

public interface IMergeSecurityManager
{
	void checkMergeAccess(ICUDResult cudResult, IMethodDescription methodDescription);
}
