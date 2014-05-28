package de.osthus.ambeth.template;

import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IRevertChangesHelper;
import de.osthus.ambeth.model.IDataObject;

public class DataObjectTemplate
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
