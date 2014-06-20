package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

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
