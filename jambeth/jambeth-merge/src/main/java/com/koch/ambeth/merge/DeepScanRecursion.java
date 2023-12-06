package com.koch.ambeth.merge;

import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IdentityHashSet;

import java.util.List;
import java.util.Optional;

public class DeepScanRecursion implements IDeepScanRecursion {
    @Override
    public void handleDeep(Object obj, EntityDelegate entityDelegate) {
        handleDeep(obj, new DeepScanState(entityDelegate));
    }

    protected boolean handleDeep(Object obj, DeepScanState state) {
        if (obj == null || !state.alreadyHandledObjectsSet.add(obj)) {
            return true;
        }
        if (obj instanceof List) {
            var list = (List<?>) obj;
            for (int a = 0, size = list.size(); a < size; a++) {
                if (!handleDeep(list.get(a), state)) {
                    return false;
                }
            }
            return true;
        } else if (obj instanceof Iterable) {
            for (var item : (Iterable<?>) obj) {
                if (!handleDeep(item, state)) {
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
                if (!handleDeep(item, state)) {
                    return false;
                }
            }
            return true;
        } else if (obj instanceof Optional opt) {
            if (!opt.isPresent()) {
                return true;
            }
            return handleDeep(opt.get(), state);
        }
        if (obj instanceof IEntityMetaDataHolder) {
            return state.entityDelegate.visitEntity(obj, state);
        }
        if (obj instanceof IObjRef objRef) {
            return state.entityDelegate.visitEntityRef(objRef, state);
        }
        return true;
    }

    public class DeepScanState implements Proceed {
        public final IdentityHashSet<Object> alreadyHandledObjectsSet = new IdentityHashSet<>();

        public EntityDelegate entityDelegate;

        public DeepScanState(EntityDelegate entityDelegate) {
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
