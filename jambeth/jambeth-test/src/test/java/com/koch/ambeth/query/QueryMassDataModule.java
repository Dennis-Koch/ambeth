package com.koch.ambeth.query;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilderExtendable;
import com.koch.ambeth.util.proxy.IProxyFactory;

import java.util.Collection;

public class QueryMassDataModule implements IInitializingModule {
    public static final String ROW_COUNT = "QueryMassdataTest.rowcount";

    @Autowired
    protected IProxyFactory proxyFactory;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean(IQueryEntityCRUD.class).autowireable(IQueryEntityCRUD.class);

        var queryMassDataBuilder = beanContextFactory.registerBean(QueryMassDataBuilder.class);
        beanContextFactory.link(queryMassDataBuilder).to(IDatasetBuilderExtendable.class);

        var queryBeanBC = beanContextFactory.registerBean("myQuery1", QueryBean.class);
        queryBeanBC.propertyValue("EntityType", QueryEntity.class);
        queryBeanBC.propertyValue("QueryCreator", new IQueryCreator() {
            @Override
            public <T> IQuery<T> createCustomQuery(IQueryBuilder<T> qb) {
                return qb.build();
            }
        });
    }

    public static class QueryMassDataBuilder extends AbstractDatasetBuilder {
        @Property(name = ROW_COUNT)
        protected int dataCount;

        @Override
        public Collection<Class<? extends IDatasetBuilder>> getDependsOn() {
            return null;
        }

        @Override
        protected void buildDatasetInternal() {
            createEntity(QueryEntity.class, dataCount);
        }
    }
}
