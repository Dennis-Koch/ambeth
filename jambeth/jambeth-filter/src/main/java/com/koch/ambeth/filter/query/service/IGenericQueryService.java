package com.koch.ambeth.filter.query.service;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;

public interface IGenericQueryService {
	<T> IPagingResponse<T> filter(IFilterDescriptor<T> filterDescriptor,
			ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest);
}
