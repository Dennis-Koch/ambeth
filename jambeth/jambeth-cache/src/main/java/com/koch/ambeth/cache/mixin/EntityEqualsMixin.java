package com.koch.ambeth.cache.mixin;

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

import com.koch.ambeth.cache.proxy.IEntityEquals;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class EntityEqualsMixin {
    public boolean equals(IEntityEquals left, Object right) {
        if (right == left) {
            return true;
        }
        if (!(right instanceof IEntityEquals)) {
            return false;
        }
        var id = left.get__Id();
        if (id == null) {
            // Null id can never be equal with something other than itself
            return false;
        }
        var other = (IEntityEquals) right;
        return id.equals(other.get__Id()) && left.get__BaseType().equals(other.get__BaseType());
    }

    public int hashCode(IEntityEquals left) {
        var id = left.get__Id();
        if (id == null) {
            return System.identityHashCode(left);
        }
        return left.get__BaseType().hashCode() ^ id.hashCode();
    }

    public String toString(IEntityEquals left, IPrintable printable) {
        var sb = new StringBuilder();
        printable.toString(sb);
        return sb.toString();
    }

    public void toString(IEntityEquals left, StringBuilder sb) {
        sb.append(left.get__BaseType().getName()).append('-');
        StringBuilderUtil.appendPrintable(sb, left.get__Id());
    }
}
