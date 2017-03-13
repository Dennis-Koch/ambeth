package com.koch.ambeth.datachange.filter;

import java.util.List;

import com.koch.ambeth.datachange.model.IDataChangeEntry;

public interface IFilterRegistry
{
	List<String> evaluateMatchingTopics(IDataChangeEntry dataChangeEntry);
}
