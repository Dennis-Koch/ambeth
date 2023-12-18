package com.koch.ambeth.merge;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.metadata.MemberTypeProvider;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.ArrayList;
import lombok.SneakyThrows;

import java.io.Writer;
import java.util.List;

public class EntityMetaDataClient implements IEntityMetaDataProvider {
    @Autowired
    protected ICache cache;

    @Autowired
    protected IMergeService mergeService;

    @Autowired
    protected IProxyHelper proxyHelper;

    @Override
    public IEntityMetaData getMetaData(Class<?> entityType) {
        return getMetaData(entityType, false);
    }

    @Override
    public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly) {
        var entityTypes = new ArrayList<Class<?>>(1);
        entityTypes.add(entityType);
        var metaData = getMetaData(entityTypes);
        if (!metaData.isEmpty()) {
            return metaData.get(0);
        }
        if (tryOnly) {
            return null;
        }
        throw new IllegalArgumentException("No metadata found for entity of type " + entityType.getName());
    }

    @Override
    public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
        var realEntityTypes = new ArrayList<Class<?>>(entityTypes.size());
        for (var entityType : entityTypes) {
            realEntityTypes.add(proxyHelper.getRealType(entityType));
        }
        var cache = this.cache.getCurrentCache();
        var readLock = cache != null ? cache.getReadLock() : null;
        var lockState = readLock != null ? readLock.releaseAllLocks() : null;
        try {
            var serviceResult = mergeService.getMetaData(realEntityTypes);
            var result = new ArrayList<IEntityMetaData>();
            result.addAll(serviceResult);
            return result;
        } finally {
            if (readLock != null) {
                readLock.reacquireLocks(lockState);
            }
        }
    }

    @Override
    public List<Class<?>> findMappableEntityTypes() {
        throw new UnsupportedOperationException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
        return mergeService.getValueObjectConfig(valueType);
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(String xmlTypeName) {
        throw new UnsupportedOperationException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
    }

    @Override
    public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType) {
        throw new UnsupportedOperationException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
    }

    @Override
    public Class<?>[] getEntityPersistOrder() {
        return MemberTypeProvider.EMPTY_TYPES;
    }

    @SneakyThrows
    @Override
    public void toDotGraph(Writer writer) {
        var dot = mergeService.createMetaDataDOT();
        writer.write(dot);
    }
}
