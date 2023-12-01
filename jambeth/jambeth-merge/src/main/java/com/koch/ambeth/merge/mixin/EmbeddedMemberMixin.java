package com.koch.ambeth.merge.mixin;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.IBytecodePrinter;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.FastConstructorAccess;
import lombok.SneakyThrows;

public class EmbeddedMemberMixin {
    protected final SmartCopyMap<Class<?>, FastConstructorAccess> typeToEmbbeddedParamConstructorMap = new SmartCopyMap<>(0.5f);
    @Autowired
    protected IBytecodeEnhancer bytecodeEnhancer;
    @Autowired(optional = true)
    protected IBytecodePrinter bytecodePrinter;

    @SneakyThrows
    public Object createEmbeddedObject(Class<?> embeddedType, Class<?> entityType, Object parentObject, String memberPath) {
        var enhancedEmbeddedType = bytecodeEnhancer.getEnhancedType(embeddedType, new EmbeddedEnhancementHint(entityType, parentObject.getClass(), memberPath));
        var embeddedConstructor = getEmbeddedParamConstructor(enhancedEmbeddedType, parentObject.getClass());
        var constructorArgs = new Object[] { parentObject };
        return embeddedConstructor.newInstance(constructorArgs);
    }

    protected FastConstructorAccess getEmbeddedParamConstructor(Class<?> embeddedType, Class<?> parentObjectType) {
        var constructor = typeToEmbbeddedParamConstructorMap.get(embeddedType);
        if (constructor == null) {
            try {
                constructor = FastConstructorAccess.get(embeddedType.getConstructor(new Class<?>[] { parentObjectType }));
            } catch (Throwable e) {
                if (bytecodePrinter != null) {
                    throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(embeddedType));
                }
                throw RuntimeExceptionUtil.mask(e);
            }
            typeToEmbbeddedParamConstructorMap.put(embeddedType, constructor);
        }
        return constructor;
    }
}
