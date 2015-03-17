package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class ConstructorInstance extends MethodInstance
{
	public static final String CONSTRUCTOR_NAME = "<init>";

	public static final ConstructorInstance defaultConstructor;

	static
	{
		try
		{
			defaultConstructor = new ConstructorInstance(Object.class.getDeclaredConstructor());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static final String getSignature(java.lang.reflect.Constructor<?> method)
	{
		try
		{
			java.lang.reflect.Method getSignature = method.getClass().getDeclaredMethod("getSignature");
			getSignature.setAccessible(true);
			return (String) getSignature.invoke(method);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public ConstructorInstance(java.lang.reflect.Constructor<?> constructor)
	{
		super(Type.getType(constructor.getDeclaringClass()), TypeUtil.getModifiersToAccess(constructor.getModifiers()), Type.VOID_TYPE, CONSTRUCTOR_NAME,
				getSignature(constructor), TypeUtil.getClassesToTypes(constructor.getParameterTypes()));
	}

	public ConstructorInstance(Type owner, int access, String signature, Type... parameters)
	{
		super(owner, access, Type.VOID_TYPE, CONSTRUCTOR_NAME, signature, parameters);
	}

	public ConstructorInstance(Class<?> owner, int access, String signature, Class<?>... parameters)
	{
		super(Type.getType(owner), access, Type.VOID_TYPE, CONSTRUCTOR_NAME, signature, TypeUtil.getClassesToTypes(parameters));
	}

	public ConstructorInstance(int access, String signature, Type... parameters)
	{
		super(BytecodeBehaviorState.getState().getNewType(), access, Type.VOID_TYPE, CONSTRUCTOR_NAME, signature, parameters);
	}

	public ConstructorInstance(int access, String signature, Class<?>... parameters)
	{
		super(BytecodeBehaviorState.getState().getNewType(), access, Type.VOID_TYPE, CONSTRUCTOR_NAME, signature, TypeUtil.getClassesToTypes(parameters));
	}

	@Override
	public MethodInstance deriveName(String methodName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ConstructorInstance deriveAccess(int access)
	{
		return new ConstructorInstance(access, getSignature(), getParameters());
	}

	@Override
	public MethodInstance deriveOwner()
	{
		throw new UnsupportedOperationException();
	}
}