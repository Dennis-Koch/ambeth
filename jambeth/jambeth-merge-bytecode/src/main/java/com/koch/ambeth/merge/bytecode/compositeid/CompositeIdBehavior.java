package com.koch.ambeth.merge.bytecode.compositeid;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.merge.bytecode.visitor.CompositeIdCreator;
import com.koch.ambeth.merge.proxy.ICompositeId;
import org.objectweb.asm.ClassVisitor;

import java.util.List;

public class CompositeIdBehavior extends AbstractBehavior {

    @Override
    public Class<?>[] getEnhancements() {
        return new Class[] { ICompositeId.class };
    }

    @Override
    public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors, List<IBytecodeBehavior> cascadePendingBehaviors) {
        if (state.getContext(CompositeIdEnhancementHint.class) == null) {
            return visitor;
        }
        visitor = new CompositeIdCreator(visitor);
        return visitor;
    }
}
