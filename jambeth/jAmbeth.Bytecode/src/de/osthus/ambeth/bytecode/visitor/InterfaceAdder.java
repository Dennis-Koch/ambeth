package de.osthus.ambeth.bytecode.visitor;

import java.util.Set;

import net.sf.cglib.asm.Type;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;

public class InterfaceAdder extends ClassVisitor
{
	private final Set<String> newInterfaces;

	public InterfaceAdder(ClassVisitor cv, Set<String> newInterfaces)
	{
		super(Opcodes.ASM4, cv);
		this.newInterfaces = newInterfaces;
	}

	public InterfaceAdder(ClassVisitor cv, String... newInterfaces)
	{
		super(Opcodes.ASM4, cv);
		this.newInterfaces = new HashSet<String>(newInterfaces);
	}

	public InterfaceAdder(ClassVisitor cv, Class<?>... newInterfaces)
	{
		super(Opcodes.ASM4, cv);
		this.newInterfaces = new HashSet<String>(newInterfaces.length);
		for (Class<?> newInterface : newInterfaces)
		{
			this.newInterfaces.add(Type.getInternalName(newInterface));
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		LinkedHashSet<String> ints = new LinkedHashSet<String>(interfaces);
		ints.addAll(newInterfaces);
		Class<?> type = BytecodeBehaviorState.getState().getCurrentType();
		while (type != null && type != Object.class)
		{
			for (Class<?> alreadyImplementedInterface : type.getInterfaces())
			{
				String aiiName = Type.getInternalName(alreadyImplementedInterface);
				ints.remove(aiiName);
			}
			type = type.getSuperclass();
		}
		super.visit(version, access, name, signature, superName, ints.toArray(String.class));
	}
}
