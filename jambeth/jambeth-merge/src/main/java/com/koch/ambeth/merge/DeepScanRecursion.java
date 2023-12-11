package com.koch.ambeth.merge;

import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IdentityHashSet;

import java.util.List;
import java.util.Optional;

public class DeepScanRecursion implements IDeepScanRecursion {

    @Override
    public void handleDeep(Object obj, EntityDelegate entityDelegate, boolean visitEntityOnlyOnce) {
        handleDeep(obj, new DeepScanState(entityDelegate, visitEntityOnlyOnce));
    }

    protected boolean handleDeep(Object obj, DeepScanState state) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Optional opt) {
            if (!opt.isPresent()) {
                return true;
            }
            obj = opt.get();
        }
        if (state.objectAlreadyHandled(obj)) {
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
        private final IdentityHashSet<Object> alreadyHandledObjectsSet;

        public EntityDelegate entityDelegate;

        public DeepScanState(EntityDelegate entityDelegate, boolean visitEntityOnlyOnce) {
            this.entityDelegate = entityDelegate;
            alreadyHandledObjectsSet = visitEntityOnlyOnce ? new IdentityHashSet<>() : null;
        }

        @Override
        public boolean proceed(Object obj) {
            return handleDeep(obj, this);
        }

        @Override
        public boolean proceed(Object obj, EntityDelegate entityDelegate) {
            var oldEntityDelegate = this.entityDelegate;
            if (oldEntityDelegate == entityDelegate) {
                return handleDeep(obj, this);
            }
            this.entityDelegate = entityDelegate;
            try {
                return handleDeep(obj, this);
            } finally {
                this.entityDelegate = oldEntityDelegate;
            }
        }

        public boolean objectAlreadyHandled(Object obj) {
            if (alreadyHandledObjectsSet == null) {
                return false;
            }
            return !alreadyHandledObjectsSet.add(obj);
        }
    }
}
