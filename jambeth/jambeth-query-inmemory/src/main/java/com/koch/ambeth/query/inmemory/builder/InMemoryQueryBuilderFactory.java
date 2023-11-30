package com.koch.ambeth.query.inmemory.builder;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;

public class InMemoryQueryBuilderFactory implements IQueryBuilderFactory {
    @Autowired
    protected IServiceContext beanContext;

    @SuppressWarnings("unchecked")
    @Override
    public <T> IQueryBuilder<T> create(Class<T> entityType) {
        return beanContext.registerBean(InMemoryQueryBuilder.class).propertyValue("EntityType", entityType).finish();
    }
}
