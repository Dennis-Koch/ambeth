package com.koch.ambeth.persistence.blueprint;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.annotation.Find;

import java.util.List;

@MergeContext
public class EntityTypeBluePrintService implements IInitializingBean {
    @Autowired
    protected IQueryBuilderFactory qbf;
    @Autowired
    protected IPrefetchHelper prefetchHelper;
    protected IQuery<EntityTypeBlueprint> qAll;
    protected IQuery<EntityTypeBlueprint> qByName;
    protected IPrefetchHandle typeToAllPrefetchHandle;
    @LogInstance
    private ILogger log;

    @Find
    public List<EntityTypeBlueprint> getAll() {
        List<EntityTypeBlueprint> list = qAll.retrieve();
        typeToAllPrefetchHandle.prefetch(list);
        return list;
    }

    @Find
    public EntityTypeBlueprint findByName(String name) {
        EntityTypeBlueprint entityTypeBlueprint = qByName.param(IEntityTypeBlueprint.NAME, name).retrieveSingle();
        typeToAllPrefetchHandle.prefetch(entityTypeBlueprint);
        return entityTypeBlueprint;
    }

    @Override
    public void afterPropertiesSet() throws Throwable {

        IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
        EntityTypeBlueprint plan = prefetchConfig.plan(EntityTypeBlueprint.class);
        plan.getProperties().iterator().next().getAnnotations().iterator().next().getProperties().iterator().next();
        plan.getAnnotations().iterator().next().getProperties().iterator().next();
        typeToAllPrefetchHandle = prefetchConfig.build();

        IQueryBuilder<EntityTypeBlueprint> qb = qbf.create(EntityTypeBlueprint.class);
        qAll = qb.build();

        qb = qbf.create(EntityTypeBlueprint.class);
        qByName = qb.build(qb.let(qb.property(IEntityTypeBlueprint.NAME)).isEqualTo(qb.parameterValue(IEntityTypeBlueprint.NAME)));

    }
}
