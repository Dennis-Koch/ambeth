package com.koch.ambeth.datachange.persistence.services;

import com.koch.ambeth.datachange.model.IDataChange;

public interface IDataChangeEventService
{
	void save(IDataChange dataChange);
}
