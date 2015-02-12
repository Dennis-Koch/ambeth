package de.osthus.ambeth.persistence.jdbc.lob;

import de.osthus.ambeth.copy.IObjectCopierExtension;
import de.osthus.ambeth.copy.IObjectCopierState;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class BlobInputSourceObjectCopier implements IObjectCopierExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object deepClone(Object original, IObjectCopierState objectCopierState)
	{
		return new BlobInputSource(((BlobInputSource) original).lobInputSourceController);
	}
}
