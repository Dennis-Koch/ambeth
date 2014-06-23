package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Constructor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.TypeUtil;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;

/**
 * PublicConstructorVisitor declares constructors derived from {@link #superClass}.
 * 
 * {@link #superClass} is defined either by {@link BytecodeBehaviorState#getCurrentType()} or by the parameter extendedType of the
 * {@link PublicConstructorVisitor#PublicConstructorVisitor(ClassVisitor, Class)} constructor. In the latter case extendedType can be an abstract type used as
 * template to implement the interface defined by {@link BytecodeBehaviorState#getOriginalType()}.
 * 
 * If {@link #superClass} does not declare constructors or {@link #superClass} is an interface a default constructor is created.
 */
public class PublicConstructorVisitor extends ClassGenerator
{
	public static boolean hasValidConstructor()
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();

		Constructor<?>[] constructors = state.getCurrentType().getDeclaredConstructors();

		for (Constructor<?> constructor : constructors)
		{
			if (state.isMethodAlreadyImplementedOnNewType(new ConstructorInstance(constructor)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Derives constructors from {@link BytecodeBehaviorState#getState()#getCurrentType()}
	 * 
	 * @param cv
	 *            ClassVisitor
	 */
	public PublicConstructorVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd()
	{
		if (!hasValidConstructor())
		{
			IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
			Constructor<?>[] constructors = state.getCurrentType().getDeclaredConstructors();

			for (Constructor<?> constructor : constructors)
			{
				int access = TypeUtil.getModifiersToAccess(constructor.getModifiers());
				access &= ~Opcodes.ACC_PROTECTED;
				access &= ~Opcodes.ACC_PRIVATE;
				access |= Opcodes.ACC_PUBLIC;
				ConstructorInstance c_method = new ConstructorInstance(constructor.getDeclaringClass(), access, ConstructorInstance.getSignature(constructor),
						constructor.getParameterTypes());

				MethodGenerator mg = visitMethod(c_method);
				mg.loadThis();
				mg.loadArgs();
				mg.invokeConstructor(c_method);

				mg.returnValue();
				mg.endMethod();
			}
			if (constructors.length == 0)
			{
				// Implement "first" default constructor
				ConstructorInstance c_method;
				try
				{
					c_method = new ConstructorInstance(Object.class.getConstructor());
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				MethodGenerator ga = visitMethod(c_method);
				ga.loadThis();
				ga.invokeConstructor(c_method);
				ga.returnValue();
				ga.endMethod();
			}
		}
		super.visitEnd();
	}
}