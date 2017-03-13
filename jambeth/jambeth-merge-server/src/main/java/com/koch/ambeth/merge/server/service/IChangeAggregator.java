package com.koch.ambeth.merge.server.service;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IChangeAggregator
{
	void dataChangeInsert(IObjRef reference);

	void dataChangeUpdate(IObjRef reference);

	void dataChangeDelete(IObjRef reference);

	void createDataChange();

	void clear();
}
