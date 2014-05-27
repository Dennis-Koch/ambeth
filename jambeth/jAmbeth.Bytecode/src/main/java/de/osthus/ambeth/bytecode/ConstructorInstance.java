package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class ConstructorInstance extends MethodInstance
{
	public static final String CONSTRUCTOR_NAME = "<init>";

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
		super(Type.getType(constructor.getDeclaringClass()), TypeUtil.getModifiersToAccess(constructor.getModifiers()), CONSTRUCTOR_NAME,
				getSignature(constructor), Type.VOID_TYPE, TypeUtil.getClassesToTypes(constructor.getParameterTypes()));
	}

	public ConstructorInstance(Type owner, int access, String signature, Type... parameters)
	{
		super(owner, access, CONSTRUCTOR_NAME, signature, Type.VOID_TYPE, parameters);
	}

	public ConstructorInstance(Class<?> owner, int access, String signature, Class<?>... parameters)
	{
		super(Type.getType(owner), access, CONSTRUCTOR_NAME, signature, Type.VOID_TYPE, TypeUtil.getClassesToTypes(parameters));
	}

	public ConstructorInstance(int access, String signature, Type... parameters)
	{
		super(BytecodeBehaviorState.getState().getNewType(), access, CONSTRUCTOR_NAME, signature, Type.VOID_TYPE, parameters);
	}

	public ConstructorInstance(int access, String signature, Class<?>... parameters)
	{
		super(BytecodeBehaviorState.getState().getNewType(), access, CONSTRUCTOR_NAME, signature, Type.VOID_TYPE, TypeUtil.getClassesToTypes(parameters));
	}
}