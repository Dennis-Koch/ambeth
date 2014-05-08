package de.osthus.ambeth.services;

import java.util.List;

import de.osthus.ambeth.annotation.NoProxy;
import de.osthus.ambeth.model.DataChangeEventBO;

public interface IDataChangeEventDAO
{
	void save(DataChangeEventBO dataChangeEvent);

	List<DataChangeEventBO> retrieveAll();

	@NoProxy
	void removeBefore(long time);
}
