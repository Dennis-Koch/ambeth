package com.koch.ambeth.merge;

import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.util.collections.IdentityHashSet;

import java.util.List;

public class DeepScanRecursion implements IDeepScanRecursion {
    @Override
    public void handleDeep(Object obj, EntityDelegate entityDelegate) {
        handleDeep(obj, new DeepScanProceedIntern(entityDelegate));
    }

    protected boolean handleDeep(Object obj, DeepScanProceedIntern deepScanProceed) {
        if (obj == null || !deepScanProceed.alreadyHandledObjectsSet.add(obj)) {
            return true;
        }
        if (obj instanceof List) {
            var list = (List<?>) obj;
            for (int a = 0, size = list.size(); a < size; a++) {
                if (!handleDeep(list.get(a), deepScanProceed)) {
                    return false;
                }
            }
            return true;
        } else if (obj instanceof Iterable) {
            for (var item : (Iterable<?>) obj) {
                if (!handleDeep(item, deepScanProceed)) {
                    return false;
                }
            }
            return true;
        } else if (obj.getClass().isArray()) {
            if (obj.getClass().getComponentType().isPrimitive()) {
                // primitive arrays can not be cast to Object[]
                return true;
            }
            // This is valid for non-native arrays in java
            var array = (Object[]) obj;
            for (int a = array.length; a-- > 0; ) {
                var item = array[a];
                if (!handleDeep(item, deepScanProceed)) {
                    return false;
                }
            }
            return true;
        }
        if (!(obj instanceof IEntityMetaDataHolder)) {
            return true;
        }
        return deepScanProceed.entityDelegate.visitEntity(obj, deepScanProceed);
    }

    public class DeepScanProceedIntern implements Proceed {
        public final IdentityHashSet<Object> alreadyHandledObjectsSet = new IdentityHashSet<>();

        public EntityDelegate entityDelegate;

        public DeepScanProceedIntern(EntityDelegate entityDelegate) {
            this.entityDelegate = entityDelegate;
        }

        @Override
        public boolean proceed(Object obj) {
            return handleDeep(obj, this);
        }

        @Override
        public boolean proceed(Object obj, EntityDelegate entityDelegate) {
            if (this.entityDelegate == entityDelegate) {
                return handleDeep(obj, this);
            }
            EntityDelegate oldEntityDelegate = this.entityDelegate;
            this.entityDelegate = entityDelegate;
            try {
                return handleDeep(obj, this);
            } finally {
                this.entityDelegate = oldEntityDelegate;
            }
        }
    }
}
