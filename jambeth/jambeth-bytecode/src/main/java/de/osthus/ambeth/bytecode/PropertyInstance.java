package de.osthus.ambeth.bytecode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.repackaged.org.objectweb.asm.AnnotationVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.util.ReflectUtil;

public class PropertyInstance
{
	public static PropertyInstance findByTemplate(IPropertyInfo propertyTemplate)
	{
		return findByTemplate(propertyTemplate, false);
	}

	public static PropertyInstance findByTemplate(IPropertyInfo propertyTemplate, boolean tryOnly)
	{
		return findByTemplate(propertyTemplate.getName(), tryOnly);
	}

	public static PropertyInstance findByTemplate(PropertyInstance propertyTemplate, boolean tryOnly)
	{
		return findByTemplate(propertyTemplate.getName(), tryOnly);
	}

	public static PropertyInstance findByTemplate(Class<?> declaringType, String propertyName, boolean tryOnly)
	{
		Method getter = ReflectUtil.getDeclaredMethod(true, declaringType, "get" + propertyName, new Type[0]);
		if (getter == null)
		{
			getter = ReflectUtil.getDeclaredMethod(true, declaringType, "is" + propertyName, new Type[0]);
		}
		Method setter = ReflectUtil.getDeclaredMethod(true, declaringType, "set" + propertyName, new Type[1]);
		if (getter != null)
		{
			MethodInstance getterInstance = new MethodInstance(getter);
			MethodInstance setterInstance = null;
			if (setter != null)
			{
				setterInstance = new MethodInstance(setter);
			}
			return new PropertyInstance(getterInstance.getOwner(), propertyName, getterInstance, setterInstance);
		}
		else if (setter != null)
		{
			MethodInstance setterInstance = new MethodInstance(setter);
			return new PropertyInstance(setterInstance.getOwner(), propertyName, null, setterInstance);
		}
		// last chance: check the propertyName directly
		Method getterOrSetter = ReflectUtil.getDeclaredMethod(true, declaringType, Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1));
		if (getterOrSetter != null)
		{
			MethodInstance getterOrSetterInstance = new MethodInstance(getterOrSetter);
			if (getterOrSetterInstance.getParameters().length == 0)
			{
				return new PropertyInstance(getterOrSetterInstance.getOwner(), propertyName, getterOrSetterInstance, null);
			}
			return new PropertyInstance(getterOrSetterInstance.getOwner(), propertyName, null, getterOrSetterInstance);
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException("No property found on class hierarchy: " + propertyName + ". Start type: " + declaringType.getName());
	}

	public static PropertyInstance findByTemplate(String propertyName, boolean tryOnly)
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		PropertyInstance pi = state.getProperty(propertyName);
		if (pi != null)
		{
			return pi;
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException("No property found on class hierarchy: " + propertyName + ". Start type: " + state.getNewType());
	}

	protected IPropertyInfo property;

	protected MethodGenerator getterGen;

	protected MethodInstance getter;

	protected MethodInstance setter;

	protected MethodGenerator setterGen;

	protected final Type owner;

	protected final String name;

	protected final Type propertyType;

	protected final String signature;

	public PropertyInstance(MethodPropertyInfo property)
	{
		this(Type.getType(property.getDeclaringType()), property.getName(), property.getGetter() != null ? new MethodInstance(property.getGetter()) : null,
				property.getSetter() != null ? new MethodInstance(property.getSetter()) : null);
		this.property = property;
	}

	public PropertyInstance(String propertyName, MethodInstance getter, MethodInstance setter)
	{
		this(BytecodeBehaviorState.getState().getNewType(), propertyName, getter, setter);
	}

	public PropertyInstance(String propertyName, String signature, Type propertyType)
	{
		this.owner = BytecodeBehaviorState.getState().getNewType();
		this.name = propertyName;
		this.signature = signature;
		this.propertyType = propertyType;
	}

	public PropertyInstance(Type owner, String propertyName, MethodInstance getter, MethodInstance setter)
	{
		this.owner = owner;
		this.name = propertyName;
		this.getter = getter;
		this.setter = setter;
		if (getter != null)
		{
			propertyType = getter.getReturnType();
			signature = getter.getSignatureFromReturnType();
		}
		else if (setter != null)
		{
			propertyType = setter.getParameters()[0];
			signature = setter.getSignatureFromParameterType(0);
		}
		else
		{
			throw new IllegalArgumentException("Either a getter or a setter must be specified");
		}
	}

	public Type getOwner()
	{
		return owner;
	}

	public boolean isConfigurable()
	{
		return BytecodeBehaviorState.getState().getNewType().equals(getOwner());
	}

	public void addAnnotation(Class<? extends Annotation> ci, Object... args)
	{
		if (!isConfigurable())
		{
			throw new IllegalArgumentException();
		}
		if (getterGen != null)
		{
			AnnotationVisitor av = getterGen.visitAnnotation(Type.getDescriptor(ci), true);
			Method[] methods = ci.getDeclaredMethods();
			for (int a = methods.length; a-- > 0;)
			{
				Method method = methods[a];
				convertArg(av, method.getName(), method.getReturnType(), args[a]);
			}
			av.visitEnd();
		}
		else if (setterGen != null)
		{
			AnnotationVisitor av = setterGen.visitAnnotation(Type.getDescriptor(ci), true);
			Method[] methods = ci.getDeclaredMethods();
			for (int a = methods.length; a-- > 0;)
			{
				Method method = methods[a];
				convertArg(av, method.getName(), method.getReturnType(), args[a]);
			}
			av.visitEnd();
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	protected void convertArg(AnnotationVisitor av, String name, Class<?> expectedType, Object arg)
	{
		if (expectedType.isArray())
		{
			AnnotationVisitor ava = av.visitArray(name);
			if (arg.getClass().isArray())
			{
				for (int a = 0, size = Array.getLength(arg); a < size; a++)
				{
					ava.visit(null, Array.get(arg, a));
				}
			}
			else
			{
				ava.visit(null, arg);
			}
			ava.visitEnd();
			return;
		}
		else if (expectedType.isAssignableFrom(arg.getClass()))
		{
			av.visit(name, arg);
			return;
		}
		throw new IllegalArgumentException("Can not convert " + arg.getClass() + " to " + expectedType);
	}

	public MethodInstance getGetter()
	{
		return getter;
	}

	public void setGetter(MethodInstance value)
	{
		if (value != null)
		{
			getter = value;
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public void setGetterGen(MethodGenerator value)
	{
		getterGen = value;
	}

	public void setSetterGen(MethodGenerator value)
	{
		setterGen = value;
	}

	public MethodInstance getSetter()
	{
		return setter;
	}

	public void setSetter(MethodInstance value)
	{
		if (value != null)
		{
			setter = value;
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public String getSignature()
	{
		return signature;
	}

	public Type getPropertyType()
	{
		return propertyType;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (getGetter() != null)
		{
			int access = getGetter().getAccess();
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
				throw new IllegalStateException("No visibility for method defined: " + getGetter());
			}
			if ((access & Opcodes.ACC_STATIC) != 0)
			{
				sb.append("static ");
			}
			if ((access & Opcodes.ACC_FINAL) != 0)
			{
				sb.append("final ");
			}
			sb.append(getPropertyType().getClassName()).append(' ');
			sb.append(getName());
			sb.append(" { get; ");
			if (getSetter() == null)
			{
				sb.append('}');
			}
			else
			{
				sb.append("set; }");
			}
		}
		else if (getSetter() == null)
		{
			sb.append(getPropertyType().getClassName()).append(' ');
			sb.append(getName());
			sb.append("{}");
		}
		else
		{
			sb.append(getPropertyType().getClassName()).append(' ');
			sb.append(getName());
			sb.append("{ set; }");
		}
		return sb.toString();
	}
}
