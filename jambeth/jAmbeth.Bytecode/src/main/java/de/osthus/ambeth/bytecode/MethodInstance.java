package de.osthus.ambeth.bytecode;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.Method;
import de.osthus.ambeth.repackaged.org.objectweb.asm.signature.SignatureReader;
import de.osthus.ambeth.repackaged.org.objectweb.asm.signature.SignatureVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.signature.SignatureWriter;
import de.osthus.ambeth.util.ReflectUtil;

public class MethodInstance
{
	public static final String getSignature(java.lang.reflect.Method method)
	{
		try
		{
			java.lang.reflect.Method getGenericSignature = method.getClass().getDeclaredMethod("getGenericSignature");
			getGenericSignature.setAccessible(true);
			return (String) getGenericSignature.invoke(method);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static final String buildSetterSignatureFromGetterSignature(String getterSignature)
	{
		if (getterSignature == null)
		{
			return null;
		}
		SignatureWriter sw = new SignatureWriter();

		SignatureReader sr = new SignatureReader(getterSignature);

		{
			SignatureVisitor pw = sw.visitParameterType();

			sr.accept(pw);

			pw.visitEnd();
		}
		return sw.toString();
	}

	public static MethodInstance findByTemplate(java.lang.reflect.Method methodTemplate)
	{
		return findByTemplate(methodTemplate, false);
	}

	public static MethodInstance findByTemplate(java.lang.reflect.Method methodTemplate, boolean tryOnly)
	{
		return findByTemplate(new MethodInstance(methodTemplate), tryOnly);
	}

	public static MethodInstance findByTemplate(MethodInstance methodTemplate, boolean tryOnly)
	{
		return findByTemplate(tryOnly, methodTemplate.getName(), methodTemplate.getParameters());
	}

	public static MethodInstance findByTemplate(boolean tryOnly, String methodName, Type... parameters)
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		for (MethodInstance methodOnNewType : state.getAlreadyImplementedMethodsOnNewType())
		{
			if (!methodOnNewType.getName().equals(methodName))
			{
				continue;
			}
			Type[] paramsOnNewType = methodOnNewType.getParameters();
			if (paramsOnNewType.length != parameters.length)
			{
				continue;
			}
			boolean paramsEqual = true;
			for (int a = paramsOnNewType.length; a-- > 0;)
			{
				if (!paramsOnNewType[a].equals(parameters[a]))
				{
					paramsEqual = false;
					break;
				}
			}
			if (paramsEqual)
			{
				return methodOnNewType;
			}
		}
		Class<?> currType = state.getCurrentType();
		if (!currType.isInterface())
		{
			while (currType != null && currType != Object.class)
			{
				java.lang.reflect.Method method = ReflectUtil.getDeclaredMethod(true, currType, methodName, parameters);
				if (method != null)
				{
					if ((method.getModifiers() & Modifier.ABSTRACT) != 0)
					{
						// Method found but it is abstract. So it is not a callable instance
						break;
					}
					return new MethodInstance(method);
				}
				currType = currType.getSuperclass();
			}
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalStateException("No method found on class hierarchy: " + methodName + ". Start type: " + state.getNewType());
	}

	protected final Type owner;

	protected final Method method;

	protected final int access;

	protected final String signature;

	public MethodInstance(Type owner, Class<?> declaringTypeOfMethod, String methodName, Class<?>... parameters)
	{
		this(owner != null ? owner : Type.getType(declaringTypeOfMethod), ReflectUtil.getDeclaredMethod(false, declaringTypeOfMethod, methodName, parameters));
	}

	public MethodInstance(java.lang.reflect.Method method)
	{
		this(Type.getType(method.getDeclaringClass()), method);
	}

	public MethodInstance(Type owner, java.lang.reflect.Method method)
	{
		this(owner, TypeUtil.getModifiersToAccess(method.getModifiers()), method.getName(), getSignature(method), Type.getType(method.getReturnType()),
				TypeUtil.getClassesToTypes(method.getParameterTypes()));
	}

	public MethodInstance(Type owner, MethodInstance superMethod)
	{
		this(owner, superMethod.getAccess(), superMethod.getName(), superMethod.getSignature(), superMethod.getDescriptor());
	}

	public MethodInstance(Class<?> owner, int access, String name, String signature, Class<?> returnType, Class<?>... parameters)
	{
		this(owner != null ? Type.getType(owner) : null, access, name, signature, Type.getType(returnType), TypeUtil.getClassesToTypes(parameters));
	}

	public MethodInstance(Type owner, int access, String name, String signature, String desc)
	{
		this(owner, access, new Method(name, desc), signature);
	}

	public MethodInstance(Type owner, int access, Method method, String signature)
	{
		this.owner = owner;
		this.access = access;
		this.method = method;
		this.signature = signature;
	}

	public MethodInstance(Type owner, int access, String name, String signature, Type returnType, Type... parameters)
	{
		super();
		this.owner = owner;
		this.access = access;
		this.signature = signature;
		StringBuilder sb = new StringBuilder();
		sb.append(returnType.getClassName()).append(' ');
		sb.append(name).append(" (");
		for (int a = 0, size = parameters.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append(", ");
			}
			sb.append(parameters[a].getClassName());
		}
		sb.append(')');
		this.method = Method.getMethod(sb.toString());
	}

	public MethodInstance deriveOwner()
	{
		return new MethodInstance(BytecodeBehaviorState.getState().getNewType(), this);
	}

	public MethodInstance deriveName(String methodName)
	{
		return new MethodInstance(getOwner(), getAccess(), methodName, getSignature(), getReturnType(), getParameters());
	}

	public Type getOwner()
	{
		return owner;
	}

	public int getAccess()
	{
		return access;
	}

	public Method getMethod()
	{
		return method;
	}

	public String getSignature()
	{
		return signature;
	}

	public String getSignatureFromParameterType(int parameterIndex)
	{
		return FieldInstance.getSignatureFromParameterType(getSignature(), parameterIndex);
	}

	public String getSignatureFromReturnType()
	{
		return FieldInstance.getSignatureFromReturnType(getSignature());
	}

	public Type getReturnType()
	{
		return method.getReturnType();
	}

	public Type[] getParameters()
	{
		return method.getArgumentTypes();
	}

	public String getName()
	{
		return method.getName();
	}

	public String getDescriptor()
	{
		return method.getDescriptor();
	}

	public boolean equalsSignature(MethodInstance method)
	{
		return getName().equals(method.getName()) && Arrays.equals(getParameters(), method.getParameters());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if ((access & Opcodes.ACC_PUBLIC) != 0)
		{
			sb.append("public ");
		}
		else if ((access & Opcodes.ACC_PROTECTED) != 0)
		{
			sb.append("protected ");
		}
		else if ((access & Opcodes.ACC_PRIVATE) != 0)
		{
			sb.append("private ");
		}
		else
		{
			throw new IllegalStateException("No visibility for method defined: " + method);
		}
		if ((access & Opcodes.ACC_STATIC) != 0)
		{
			sb.append("static ");
		}
		if ((access & Opcodes.ACC_FINAL) != 0)
		{
			sb.append("final ");
		}
		sb.append(method.getReturnType().getClassName()).append(' ');
		if (owner != null)
		{
			sb.append(owner.getClassName()).append('.');
		}
		sb.append(method.getName()).append('(');
		Type[] parameters = method.getArgumentTypes();
		for (int a = 0, size = parameters.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append(',');
			}
			sb.append(parameters[a].getClassName());
		}
		sb.append(')');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof MethodInstance))
		{
			return false;
		}
		MethodInstance other = (MethodInstance) obj;
		return getOwner().equals(other.getOwner()) && getMethod().equals(other.getMethod());
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getOwner().hashCode() ^ getMethod().hashCode();
	}
}
