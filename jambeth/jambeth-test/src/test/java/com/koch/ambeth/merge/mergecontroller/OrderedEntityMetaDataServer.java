package com.koch.ambeth.merge.mergecontroller;

import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;

import java.io.Writer;
import java.util.List;

public class OrderedEntityMetaDataServer implements IEntityMetaDataProvider {
    private final Class<?>[] entityPersistOrder = { Parent.class, Child.class };

    private final IEntityMetaDataProvider entityMetaDataProvider;

    public OrderedEntityMetaDataServer(IEntityMetaDataProvider entityMetaDataProvider) {
        this.entityMetaDataProvider = entityMetaDataProvider;
    }

    @Override
    public Class<?>[] getEntityPersistOrder() {
        return entityPersistOrder;
    }

    @Override
    public IEntityMetaData getMetaData(Class<?> entityType) {
        return entityMetaDataProvider.getMetaData(entityType);
    }

    @Override
    public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly) {
        return entityMetaDataProvider.getMetaData(entityType, tryOnly);
    }

    @Override
    public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
        return entityMetaDataProvider.getMetaData(entityTypes);
    }

    @Override
    public List<Class<?>> findMappableEntityTypes() {
        return entityMetaDataProvider.findMappableEntityTypes();
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
        return entityMetaDataProvider.getValueObjectConfig(valueType);
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(String xmlTypeName) {
        return entityMetaDataProvider.getValueObjectConfig(xmlTypeName);
    }

    @Override
    public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType) {
        return entityMetaDataProvider.getValueObjectTypesByEntityType(entityType);
    }

    @Override
    public void toDotGraph(Writer writer) {
        entityMetaDataProvider.toDotGraph(writer);
    }
}
