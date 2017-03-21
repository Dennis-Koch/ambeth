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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.util.collections.HashSet;

public class InterfaceToClassVisitor extends ClassGenerator {
	public InterfaceToClassVisitor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName,
			String[] interfaces) {
		Class<?> originalType = getState().getOriginalType();
		access &= ~Opcodes.ACC_ABSTRACT;
		access &= ~Opcodes.ACC_INTERFACE;
		if (originalType.isInterface()) {
			HashSet<String> interfaceSet = new HashSet<>(interfaces);
			interfaceSet.add(Type.getInternalName(originalType));
			interfaces = interfaceSet.toArray(String.class);
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}
}
