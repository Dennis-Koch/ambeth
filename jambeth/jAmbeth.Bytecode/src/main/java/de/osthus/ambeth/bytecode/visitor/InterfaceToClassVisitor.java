package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Constructor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.TypeUtil;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.tree.ClassNode;

public class InterfaceToClassVisitor extends ClassGenerator
{
	protected static final Type objType = Type.getType(Object.class);

	private final ClassNode parentTypeNode;

	private String superName;

	protected boolean interfaceMode = false;

	public InterfaceToClassVisitor(ClassVisitor cv, ClassNode parentTypeNode)
	{
		super(cv);
		this.parentTypeNode = parentTypeNode;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		access &= ~Opcodes.ACC_ABSTRACT;
		if ((access & Opcodes.ACC_INTERFACE) != 0)
		{
			access &= ~Opcodes.ACC_INTERFACE;
			if ((parentTypeNode.access & Opcodes.ACC_INTERFACE) != 0)
			{
				// Move parent interface (which is wrong place) to the implemented interfaces
				HashSet<String> interfaceSet = new HashSet<String>(interfaces);
				interfaceSet.add(parentTypeNode.name);
				interfaces = interfaceSet.toArray(String.class);
				if (parentTypeNode.name.equals(superName))
				{
					superName = objType.getInternalName();
					interfaceMode = true;
				}
			}
		}
		this.superName = superName;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public void visitEnd()
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		MethodInstance[] methods = state.getAlreadyImplementedMethodsOnNewType();
		boolean constructorDefined = false;
		for (MethodInstance method : methods)
		{
			if (ConstructorInstance.CONSTRUCTOR_NAME.equals(method.getName()))
			{
				constructorDefined = true;
				break;
			}
		}
		if (!constructorDefined)
		{
			if (interfaceMode)
			{
				implementConstructor(new Class<?>[0]);
			}
			else
			{
				Constructor<?>[] constructors = state.getCurrentType().getDeclaredConstructors();
				for (Constructor<?> constructor : constructors)
				{
					implementConstructor(constructor.getParameterTypes());
				}
			}
		}
		super.visitEnd();
	}

	protected void implementConstructor(Class<?>[] parameterTypes)
	{
		Type[] types = TypeUtil.getClassesToTypes(parameterTypes);
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, types);
		MethodGenerator mg = visitMethod(Opcodes.ACC_PUBLIC, ConstructorInstance.CONSTRUCTOR_NAME, desc, null, null);
		mg.loadThis();
		mg.loadArgs();
		mg.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, ConstructorInstance.CONSTRUCTOR_NAME, desc);
		mg.returnValue();
		mg.endMethod();
	}
}
