package com.koch.ambeth.merge;

import com.koch.ambeth.util.IDelegate;

public interface RevertChangesFinishedCallback extends IDelegate
{
	void invoke(boolean success);
}
