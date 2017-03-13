package com.koch.ambeth.merge.metadata;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.copy.IObjectCopierExtension;
import com.koch.ambeth.merge.copy.IObjectCopierState;
import com.koch.ambeth.service.merge.model.IObjRef;

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
