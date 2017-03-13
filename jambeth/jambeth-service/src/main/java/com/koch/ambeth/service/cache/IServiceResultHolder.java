package com.koch.ambeth.service.cache;

import com.koch.ambeth.service.cache.model.IServiceResult;

public interface IServiceResultHolder
{
	boolean isExpectServiceResult();

	void setExpectServiceResult(boolean expectServiceResult);

	IServiceResult getServiceResult();

	void setServiceResult(IServiceResult oris);

	void clearResult();
}
