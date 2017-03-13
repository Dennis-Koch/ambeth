package com.koch.ambeth.datachange.persistence.services;

import java.util.List;

import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.util.annotation.NoProxy;

public interface IDataChangeEventDAO
{
	void save(DataChangeEventBO dataChangeEvent);

	List<DataChangeEventBO> retrieveAll();

	@NoProxy
	void removeBefore(long time);
}
