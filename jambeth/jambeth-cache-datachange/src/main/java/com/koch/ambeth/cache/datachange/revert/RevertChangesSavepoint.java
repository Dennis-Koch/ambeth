package com.koch.ambeth.cache.datachange.revert;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.util.collections.IMap;

import java.time.Instant;

public class RevertChangesSavepoint implements IRevertChangesSavepoint {
    public static final String P_CHANGES = "Changes";
    protected final Instant savepointTime = Instant.now();
    @Autowired
    protected ICacheModification cacheModification;
    @Property
    protected IMap<Object, IBackup> changes;

    @Override
    public void dispose() {
        changes = null;
    }

    @Override
    public Object[] getSavedBusinessObjects() {
        return changes.keyList().toArray(Object[]::new);
    }

    @Override
    public void revertChanges() {
        if (changes == null) {
            throw new IllegalStateException("This object has already been disposed");
        }
        var rollback = cacheModification.pushActive();
        try {
            for (var entry : changes) {
                entry.getValue().restore(entry.getKey());
            }
        } finally {
            rollback.rollback();
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        int index = 1;
        for (var entry : changes) {
            var key = entry.getKey();
            if (index > 1) {
                sb.append('\n');
            }
            sb.append(index).append(") ").append(key);
            index++;
        }
        return sb.toString();
    }
}
