package com.koch.ambeth.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class FieldInstance
{
	public static final String getSignature(java.lang.reflect.Field field)
	{
		try
		{
			java.lang.reflect.Method getGenericSignature = field.getClass().getDeclaredMethod("getGenericSignature");
			getGenericSignature.setAccessible(true);
			return (String) getGenericSignature.invoke(field);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static final String getSignatureFromReturnType(java.lang.reflect.Method method)
	{
		String methodSignature = MethodInstance.getSignature(method);
		return getSignatureFromReturnType(methodSignature);
	}

	public static final String getSignatureFromReturnType(String methodSignature)
	{
		if (methodSignature == null || methodSignature.length() == 0)
		{
			return null;
		}
		final ParamHolder<String> fieldSignature = new ParamHolder<String>();
		SignatureReader sr = new SignatureReader(methodSignature);

		sr.accept(new SignatureVisitor(Opcodes.ASM4)
		{
			@Override
			public SignatureVisitor visitReturnType()
			{
				return new SignatureWriter()
				{
					@Override
					public void visitEnd()
					{
						super.visitEnd();
						fieldSignature.setValue(super.toString());
					}
				};
			}
		});
		return fieldSignature.getValue();
	}

	public static final String getSignatureFromParameterType(java.lang.reflect.Method method, int parameterIndex)
	{
		String methodSignature = MethodInstance.getSignature(method);
		return getSignatureFromParameterType(methodSignature, parameterIndex);
	}

	public static final String getSignatureFromParameterType(MethodInstance method, int parameterIndex)
	{
		return getSignatureFromParameterType(method.getSignature(), parameterIndex);
	}

	public static final String getSignatureFromParameterType(String methodSignature, final int parameterIndex)
	{
		if (parameterIndex < 0)
		{
			throw new IllegalArgumentException("parameterIndex must be >= 0");
		}
		if (methodSignature == null || methodSignature.length() == 0)
		{
			return null;
		}
		final ParamHolder<String> fieldSignature = new ParamHolder<String>();
		SignatureReader sr = new SignatureReader(methodSignature);

		sr.accept(new SignatureVisitor(Opcodes.ASM4)
		{
			private int counter;

			@Override
			public SignatureVisitor visitParameterType()
			{
				if (counter != parameterIndex)
				{
					counter++;
					return super.visitParameterType();
				}
				return new SignatureWriter()
				{
					@Override
					public void visitEnd()
					{
						super.visitEnd();
						fieldSignature.setValue(super.toString());
					}
				};
			}
		});
		return fieldSignature.getValue();
	}

	protected final Type owner;

	protected final int access;

	protected final String name;

	protected final Type type;

	protected final String signature;

	public FieldInstance(java.lang.reflect.Field field)
	{
		this(Type.getType(field.getDeclaringClass()), TypeUtil.getModifiersToAccess(field.getModifiers()), field.getName(), getSignature(field), Type
				.getType(field.getType()));
	}

	public FieldInstance(Class<?> owner, int access, String name, String signature, Class<?> type)
	{
		this(Type.getType(owner), access, name, signature, Type.getType(type));
	}

	public FieldInstance(Type owner, int access, String name, String signature, Class<?> type)
	{
		this(owner, access, name, signature, Type.getType(type));
	}

	public FieldInstance(int access, String name, String signature, Class<?> type)
	{
		this(BytecodeBehaviorState.getState().getNewType(), access, name, signature, Type.getType(type));
	}

	public FieldInstance(int access, String name, String signature, Type type)
	{
		this(BytecodeBehaviorState.getState().getNewType(), access, name, signature, type);
	}

	public FieldInstance(Type owner, int access, String name, String signature, Type type)
	{
		this.owner = owner;
		this.access = access;
		this.name = name;
		this.type = type;
		this.signature = signature;
	}

	public Type getOwner()
	{
		return owner;
	}

	public int getAccess()
	{
		return access;
	}

	public String getName()
	{
		return name;
	}

	public Type getType()
	{
		return type;
	}

	public String getSignature()
	{
		return signature;
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
			throw new IllegalStateException("No visibility for field defined: " + name);
		}
		if ((access & Opcodes.ACC_STATIC) != 0)
		{
			sb.append("static ");
		}
		if ((access & Opcodes.ACC_FINAL) != 0)
		{
			sb.append("final ");
		}
		sb.append(getType().getClassName()).append(' ');
		if (owner != null)
		{
			sb.append(owner.getClassName()).append('.');
		}
		sb.append(name);
		return sb.toString();
	}
}
