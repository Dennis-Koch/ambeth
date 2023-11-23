package com.koch.ambeth.server.helloworld.service;

/*-
 * #%L
 * jambeth-server-helloworld
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.server.helloworld.transfer.TestEntity;
import com.koch.ambeth.server.helloworld.transfer.TestEntity2;
import com.koch.ambeth.service.proxy.Service;

import java.util.Collection;
import java.util.List;

@Service(name = "HelloWorldService", value = IHelloWorldService.class)
@SecurityContext(SecurityContextType.NOT_REQUIRED)
public class HelloWorldService implements IHelloWorldService {
    @Autowired
    protected IQueryBuilderFactory qbf;

    @Autowired
    protected IFilterToQueryBuilder filterToQueryBuilder;

    @Override
    public IPagingResponse<TestEntity> findTestEntities(IFilterDescriptor<TestEntity> filterDescriptor, ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest) {
        IPagingQuery<TestEntity> pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);

        return pagingQuery.retrieve(pagingRequest);
    }

    @Override
    @SecurityContext(SecurityContextType.AUTHENTICATED)
    public List<TestEntity> getAllTestEntities() {
        var result = qbf.create(TestEntity.class).build().retrieve();
        if (!result.isEmpty()) {
            result.get(0).setMyValue((int) Math.random() * 10000);
        }
        return result;
    }

    @Override
    @SecurityContext(SecurityContextType.AUTHORIZED)
    public List<TestEntity2> getAllTest2Entities() {
        return qbf.create(TestEntity2.class).build().retrieve();
    }

    @Override
    public double doFunnyThings(int value, String text) {
        return ((value + text.length()) % 2) + 0.3456;
    }

    @Override
    public void saveTestEntities(TestEntity... testEntities) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void saveTestEntities(Collection<TestEntity> testEntities) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void saveTest2Entities(TestEntity2... test2Entities) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void deleteTestEntities(TestEntity... testEntities) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void deleteTestEntities(Collection<TestEntity> testEntities) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void deleteTestEntities(long... ids) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void deleteTest2Entities(TestEntity2... test2Entities) {
        throw new UnsupportedOperationException("Not implemented, because this should never be called");
    }

    @Override
    public void forbiddenMethod() {
        throw new IllegalStateException("This will never occur because security will catch this call");
    }
}
