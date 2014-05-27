package de.osthus.ambeth.datachange.filter;

import java.util.List;

import de.osthus.ambeth.datachange.model.IDataChangeEntry;

public interface IFilterRegistry
{
	List<String> evaluateMatchingTopics(IDataChangeEntry dataChangeEntry);
}
