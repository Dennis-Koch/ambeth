package com.koch.ambeth.merge;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.util.model.IMethodDescription;

public interface IMergeSecurityManager
{
	void checkMergeAccess(ICUDResult cudResult, IMethodDescription methodDescription);
}
