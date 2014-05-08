package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Constructor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class PublicEmbeddedConstructorVisitor extends ClassGenerator
{
	public static final String PARENT_FIELD_NAME = "parent";

	public PublicEmbeddedConstructorVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		FieldInstance f_parent = new FieldInstance(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, "parent", null, Object.class);
		implementField(f_parent);

		Class<?> superType = BytecodeBehaviorState.getState().getCurrentType();
		Constructor<?>[] superConstructors = superType.getDeclaredConstructors();
		if (superConstructors.length == 0)
		{
			// Default constructor
			ConstructorInstance superConstructor;
			try
			{
				superConstructor = new ConstructorInstance(Object.class.getConstructor());
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			implementConstructor(f_parent, superConstructor);
		}
		else
		{
			for (Constructor<?> rSuperConstructor : superConstructors)
			{
				ConstructorInstance superConstructor = new ConstructorInstance(rSuperConstructor);
				implementConstructor(f_parent, superConstructor);
			}
		}
		super.visitEnd();
	}

	protected void implementConstructor(FieldInstance f_parent, ConstructorInstance superConstructor)
	{
		Type[] argTypes = superConstructor.getParameters();
		Type[] newArgTypes = new Type[argTypes.length + 1];
		System.arraycopy(argTypes, 0, newArgTypes, 0, argTypes.length);
		newArgTypes[argTypes.length] = Type.getType(Object.class);

		int access = superConstructor.getAccess();
		// Turn off private and protected
		access &= ~Opcodes.ACC_PRIVATE;
		access &= ~Opcodes.ACC_PROTECTED;
		// Turn on public
		access |= Opcodes.ACC_PUBLIC;

		// TODO: build new signature according to the newArgTypes

		ConstructorInstance constructor = new ConstructorInstance(access, null, newArgTypes);

		MethodGenerator mv = visitMethod(constructor);
		mv.loadThis();
		mv.invokeConstructor(superConstructor);
		mv.putThisField(f_parent, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.loadArg(0);
			}
		});
		mv.returnValue();
		mv.endMethod();
	}
}