package com.koch.ambeth.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.util.collections.HashSet;

public class InterfaceToClassVisitor extends ClassGenerator
{
	public InterfaceToClassVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		Class<?> originalType = getState().getOriginalType();
		access &= ~Opcodes.ACC_ABSTRACT;
		access &= ~Opcodes.ACC_INTERFACE;
		if (originalType.isInterface())
		{
			HashSet<String> interfaceSet = new HashSet<String>(interfaces);
			interfaceSet.add(Type.getInternalName(originalType));
			interfaces = interfaceSet.toArray(String.class);
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}
}
