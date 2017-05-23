package com.koch.ambeth.query.filter;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.query.service.IGenericQueryService;
import com.koch.ambeth.ioc.annotation.Autowired;

public class GenericQueryService implements IGenericQueryService {
	@Autowired
	IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public <T> IPagingResponse<T> filter(IFilterDescriptor<T> filterDescriptor,
			ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest) {
		IPagingQuery<T> pagingQuery =
				filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);
		return pagingQuery.retrieve(pagingRequest);
	}
}
