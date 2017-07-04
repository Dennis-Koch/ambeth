package com.koch.ambeth.query.filter;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.query.service.IGenericQueryService;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class GenericQueryService implements IGenericQueryService {
	@Autowired
	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Autowired
	protected ILightweightTransaction transaction;

	@Override
	public <T> IPagingResponse<T> filter(final IFilterDescriptor<T> filterDescriptor,
			final ISortDescriptor[] sortDescriptors, final IPagingRequest pagingRequest) {
		return transaction
				.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IPagingResponse<T>>() {
					@Override
					public IPagingResponse<T> invoke() throws Exception {
						IPagingQuery<T> pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor,
								sortDescriptors);
						return pagingQuery.retrieve(pagingRequest);
					}
				});
	}
}
