package com.koch.ambeth.persistence.jdbc.lob;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.copy.IObjectCopierExtension;
import com.koch.ambeth.merge.copy.IObjectCopierState;

public class ClobInputSourceObjectCopier implements IObjectCopierExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object deepClone(Object original, IObjectCopierState objectCopierState)
	{
		return new ClobInputSource(((ClobInputSource) original).lobInputSourceController);
	}
}
