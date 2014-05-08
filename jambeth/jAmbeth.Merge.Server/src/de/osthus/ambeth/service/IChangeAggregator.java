package de.osthus.ambeth.service;

import de.osthus.ambeth.merge.model.IObjRef;

public interface IChangeAggregator
{
	void dataChangeInsert(IObjRef reference);

	void dataChangeUpdate(IObjRef reference);

	void dataChangeDelete(IObjRef reference);

	void createDataChange();

	void clear();
}
