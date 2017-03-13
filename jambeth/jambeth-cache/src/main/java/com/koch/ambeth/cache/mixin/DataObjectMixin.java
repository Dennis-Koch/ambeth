package com.koch.ambeth.cache.mixin;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.util.model.IDataObject;

public class DataObjectMixin
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IRevertChangesHelper revertChangesHelper;

	public final void toBeUpdatedChanged(IDataObject obj, boolean previousValue, boolean currentValue)
	{
		if (previousValue && !currentValue && !cacheModification.isActiveOrFlushing())
		{
			revertChangesHelper.revertChanges(obj);
		}
	}
}
