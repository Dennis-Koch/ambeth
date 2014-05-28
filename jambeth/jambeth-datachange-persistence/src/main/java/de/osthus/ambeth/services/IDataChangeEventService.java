package de.osthus.ambeth.services;

import de.osthus.ambeth.datachange.model.IDataChange;

public interface IDataChangeEventService
{
	void save(IDataChange dataChange);
}
