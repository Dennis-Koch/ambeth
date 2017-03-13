package com.koch.ambeth.query.filter;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.ISortDescriptor;

public interface IFilterToQueryBuilder
{
	<T> IPagingQuery<T> buildQuery(IFilterDescriptor<T> filterDescriptor, ISortDescriptor[] sortDescriptors);
}