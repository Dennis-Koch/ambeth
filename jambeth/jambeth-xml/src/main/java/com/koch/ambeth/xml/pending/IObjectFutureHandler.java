package com.koch.ambeth.xml.pending;

import com.koch.ambeth.util.collections.IList;

public interface IObjectFutureHandler
{
	void handle(IList<IObjectFuture> objectFutures);
}