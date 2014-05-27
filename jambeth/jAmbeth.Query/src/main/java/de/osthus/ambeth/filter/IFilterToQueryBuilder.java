package de.osthus.ambeth.filter;

import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.ISortDescriptor;

public interface IFilterToQueryBuilder
{
	<T> IPagingQuery<T> buildQuery(IFilterDescriptor<T> filterDescriptor, ISortDescriptor[] sortDescriptors);
}