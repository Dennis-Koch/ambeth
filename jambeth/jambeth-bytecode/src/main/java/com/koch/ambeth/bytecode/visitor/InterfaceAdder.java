package com.koch.ambeth.bytecode.visitor;

/*-
 * #%L
 * jambeth-bytecode
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

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.util.collections.LinkedHashSet;

public class InterfaceAdder extends ClassVisitor {
	private final Set<String> newInterfaces;

	public InterfaceAdder(ClassVisitor cv, Set<String> newInterfaces) {
		super(Opcodes.ASM4, cv);
		this.newInterfaces = newInterfaces;
	}

	public InterfaceAdder(ClassVisitor cv, String... newInterfaces) {
		super(Opcodes.ASM4, cv);
		this.newInterfaces = new LinkedHashSet<>(newInterfaces);
	}

	public InterfaceAdder(ClassVisitor cv, Class<?>... newInterfaces) {
		super(Opcodes.ASM4, cv);
		this.newInterfaces = new LinkedHashSet<>(newInterfaces.length);
		for (Class<?> newInterface : newInterfaces) {
			this.newInterfaces.add(Type.getInternalName(newInterface));
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName,
			String[] interfaces) {
		LinkedHashSet<String> ints = new LinkedHashSet<>(interfaces);
		ints.addAll(newInterfaces);
		Class<?> type = BytecodeBehaviorState.getState().getCurrentType();
		while (type != null && type != Object.class) {
			for (Class<?> alreadyImplementedInterface : type.getInterfaces()) {
				String aiiName = Type.getInternalName(alreadyImplementedInterface);
				ints.remove(aiiName);
			}
			type = type.getSuperclass();
		}
		super.visit(version, access, name, signature, superName, ints.toArray(String.class));
	}
}
