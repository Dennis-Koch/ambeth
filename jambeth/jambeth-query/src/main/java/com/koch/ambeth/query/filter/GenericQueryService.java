package com.koch.ambeth.query.filter;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.query.service.IGenericQueryService;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.ILightweightTransaction;

public class GenericQueryService implements IGenericQueryService {
    @Autowired
    protected IFilterToQueryBuilder filterToQueryBuilder;

    @Autowired
    protected ILightweightTransaction transaction;

    @Override
    public <T> IPagingResponse<T> filter(final IFilterDescriptor<T> filterDescriptor, final ISortDescriptor[] sortDescriptors, final IPagingRequest pagingRequest) {
        return transaction.runInLazyTransaction(() -> {
            var pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);
            return pagingQuery.retrieve(pagingRequest);
        });
    }
}
