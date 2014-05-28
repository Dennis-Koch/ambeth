package de.osthus.ambeth.datachange.filter;

import de.osthus.ambeth.datachange.model.IDataChangeEntry;

public interface IFilter
{
	boolean doesFilterMatch(IDataChangeEntry dataChangeEntry);
}
