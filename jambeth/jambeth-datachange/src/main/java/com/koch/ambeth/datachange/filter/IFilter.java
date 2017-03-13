package com.koch.ambeth.datachange.filter;

import com.koch.ambeth.datachange.model.IDataChangeEntry;

public interface IFilter
{
	boolean doesFilterMatch(IDataChangeEntry dataChangeEntry);
}
