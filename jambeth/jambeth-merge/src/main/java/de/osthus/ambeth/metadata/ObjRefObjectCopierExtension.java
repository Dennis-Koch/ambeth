package de.osthus.ambeth.metadata;

import de.osthus.ambeth.copy.IObjectCopierExtension;
import de.osthus.ambeth.copy.IObjectCopierState;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;

public class ObjRefObjectCopierExtension implements IObjectCopierExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Override
	public Object deepClone(Object original, IObjectCopierState objectCopierState)
	{
		IObjRef objRef = (IObjRef) original;
		return objRefFactory.dup(objRef);
	}
}
