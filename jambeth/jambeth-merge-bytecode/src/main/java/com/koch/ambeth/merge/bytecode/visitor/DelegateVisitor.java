package com.koch.ambeth.merge.bytecode.visitor;

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

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DelegateVisitor extends ClassGenerator {
    private final IMap<Method, Method> mappedMethods;

    private final Class<?> type;

    public DelegateVisitor(ClassVisitor cv, Class<?> type, IMap<Method, Method> mappedMethods) {
        super(cv);
        this.type = type;
        this.mappedMethods = mappedMethods;
    }

    @SneakyThrows
    @Override
    public void visitEnd() {
        var methodsAsKey = new LinkedHashMap<MethodInstance, MethodInstance>();
        for (var method : ReflectUtil.getDeclaredMethodsInHierarchy(type)) {
            if (Modifier.isStatic(method.getModifiers()) || Modifier.isFinal(method.getModifiers())) {
                continue;
            }
            var mi = new MethodInstance(method);
            methodsAsKey.put(mi.deriveOwner(), mi);
        }
        var f_target = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$target", null, type));

        {
            var mg = visitMethod(new ConstructorInstance(Opcodes.ACC_PUBLIC, null, Object.class));
            mg.loadThis();
            mg.invokeOnExactOwner(new ConstructorInstance(Object.class.getConstructor()));
            mg.putThisField(f_target, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.loadArg(0);
                    mg.checkCast(f_target.getType());
                }
            });
            mg.returnValue();
            mg.endMethod();
        }
        for (var entry : mappedMethods) {
            var method = entry.getKey();
            var targetMethod = entry.getValue();

            var mi = new MethodInstance(method);
            methodsAsKey.remove(mi.deriveOwner());

            var mv = visitMethod(mi);
            mv.loadThis();
            mv.getField(f_target);
            var sourceParameterTypes = method.getParameterTypes();
            var targetParameterTypes = targetMethod.getParameterTypes();
            for (int a = 0, size = targetParameterTypes.length; a < size; a++) {
                mv.loadArg(a);
                if (sourceParameterTypes[a] != targetParameterTypes[a]) {
                    mv.checkCast(targetParameterTypes[a]);
                }
            }
            mv.invokeVirtual(new MethodInstance(targetMethod));
            mv.returnValue();
            mv.endMethod();
        }
        for (var entry : methodsAsKey) {
            var mv = visitMethod(entry.getKey());
            mv.loadThis();
            mv.getField(f_target);
            mv.loadArgs();
            mv.invokeVirtual(entry.getValue());
            mv.returnValue();
            mv.endMethod();
        }
        super.visitEnd();
    }
}
