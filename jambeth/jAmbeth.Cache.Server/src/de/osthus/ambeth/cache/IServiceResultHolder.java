package de.osthus.ambeth.cache;

import de.osthus.ambeth.cache.model.IServiceResult;

public interface IServiceResultHolder
{
	boolean isExpectServiceResult();

	void setExpectServiceResult(boolean expectServiceResult);

	IServiceResult getServiceResult();

	void setServiceResult(IServiceResult oris);

	void clearResult();
}
