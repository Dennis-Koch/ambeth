package com.koch.ambeth.cache.rootcachevalue;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
public class RootCacheValueEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint, Serializable {
    private static final long serialVersionUID = 5722369699026975653L;

    protected final Class<?> entityType;

    protected final boolean lruMode;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RootCacheValueEnhancementHint)) {
            return false;
        }
        var other = (RootCacheValueEnhancementHint) obj;
        return getEntityType().equals(other.getEntityType()) && Boolean.compare(lruMode, other.lruMode) == 0;
    }

    @Override
    public int hashCode() {
        return RootCacheValueEnhancementHint.class.hashCode() ^ getEntityType().hashCode() ^ (lruMode ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType) {
        if (RootCacheValueEnhancementHint.class.isAssignableFrom(includedHintType)) {
            return (T) this;
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getTargetName(null);
    }

    @Override
    public String getTargetName(Class<?> typeToEnhance) {
        return Type.getInternalName(entityType) + "$" + RootCacheValue.class.getSimpleName() + (lruMode ? "LRU" : "");
    }
}
