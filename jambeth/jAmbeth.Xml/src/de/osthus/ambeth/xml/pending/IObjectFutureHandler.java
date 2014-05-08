package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.collections.IList;

public interface IObjectFutureHandler
{
	void handle(IList<IObjectFuture> objectFutures);
}