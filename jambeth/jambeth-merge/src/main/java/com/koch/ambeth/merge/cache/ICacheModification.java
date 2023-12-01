package com.koch.ambeth.merge.cache;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;

public interface ICacheModification {

    default IStateRollback pushInternalUpdate(boolean internalUpdateActive) {
        var oldInternalUpdate = isInternalUpdate();
        if (oldInternalUpdate == internalUpdateActive) {
            return StateRollback.empty();
        }
        setInternalUpdate(internalUpdateActive);
        return () -> setInternalUpdate(oldInternalUpdate);
    }

    boolean isInternalUpdate();

    void setInternalUpdate(boolean internalUpdate);

    boolean isActive();

    void setActive(boolean active);

    boolean isActiveOrFlushing();

    boolean isActiveOrFlushingOrInternalUpdate();

    void queuePropertyChangeEvent(CheckedRunnable task);
}
