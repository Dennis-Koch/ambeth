package de.osthus.ambeth.bytecode;

import java.lang.reflect.Constructor;

import de.osthus.ambeth.annotation.FireTargetOnPropertyChange;
import de.osthus.ambeth.annotation.FireThisOnPropertyChange;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.FieldVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.MethodVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;

public class ClassGenerator extends ClassVisitor
{
	public static final Class<FireThisOnPropertyChange> c_fireThisOPC = FireThisOnPropertyChange.class;

	public static final Class<FireTargetOnPropertyChange> c_fireTargetOPC = FireTargetOnPropertyChange.class;

	private static final ThreadLocal<String> propertyNameTL = new ThreadLocal<String>();

	private static final ConstructorInstance c_obj;

	static
	{
		try
		{
			c_obj = new ConstructorInstance(Object.class.getConstructor());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static String getNameFromPropertyContext()
	{
		return propertyNameTL.get();
	}

	public static <R> R setPropertyContext(String propertyName, IResultingBackgroundWorkerDelegate<R> runnable)
	{
		String oldValue = propertyNameTL.get();
		propertyNameTL.set(propertyName);
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (oldValue == null)
			{
				propertyNameTL.remove();
			}
			else
			{
				propertyNameTL.set(oldValue);
			}
		}
	}

	public ClassGenerator(ClassVisitor cv)
	{
		super(Opcodes.ASM4, cv);
	}

	protected static IBytecodeBehaviorState getState()
	{
		return BytecodeBehaviorState.getState();
	}

	public FieldInstance implementStaticAssignedField(String staticFieldName, Object fieldValue)
	{
		ParamChecker.assertParamNotNull(fieldValue, "fieldValue");
		Class<?> fieldType = fieldValue.getClass();
		if (fieldValue instanceof IValueResolveDelegate)
		{
			fieldType = ((IValueResolveDelegate) fieldValue).getValueType();
		}
		return implementStaticAssignedField(staticFieldName, fieldType, fieldValue);
	}

	public FieldInstance implementStaticAssignedField(String staticFieldName, Class<?> fieldType, Object fieldValue)
	{
		FieldInstance field = new FieldInstance(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, staticFieldName, null, Type.getType(fieldType));
		field = implementField(field, null);
		if (fieldValue != null)
		{
			IValueResolveDelegate vrd = null;
			if (fieldValue instanceof IValueResolveDelegate)
			{
				vrd = (IValueResolveDelegate) fieldValue;
			}
			else
			{
				vrd = new NoOpValueResolveDelegate(fieldValue);
			}
			((BytecodeBehaviorState) getState()).queueFieldInitialization(field.getName(), vrd);
		}
		return getState().getAlreadyImplementedField(field.getName());
	}

	public PropertyInstance implementAssignedReadonlyProperty(String propertyName, Object fieldValue)
	{
		ParamChecker.assertParamNotNull(fieldValue, "fieldValue");
		FieldInstance field = implementStaticAssignedField("sf_" + propertyName, fieldValue);
		MethodInstance getter = new MethodInstance(getState().getNewType(), Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "get" + propertyName, null,
				field.getType());
		implementGetter(getter, field);
		PropertyInstance property = getState().getProperty(propertyName);
		if (property == null)
		{
			throw new IllegalStateException("Should never happen");
		}
		return property;
	}

	public FieldInstance implementField(FieldInstance field)
	{
		return implementField(field, null);
	}

	public FieldInstance implementField(FieldInstance field, FScript script)
	{
		FieldVisitor fv = visitField(field.getAccess(), field.getName(), field.getType().getDescriptor(), field.getSignature(), null);
		if (script != null)
		{
			script.execute(fv);
		}
		fv.visitEnd();
		return getState().getAlreadyImplementedField(field.getName());
	}

	@Override
	public MethodGenerator visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv instanceof MethodGenerator)
		{
			return (MethodGenerator) mv;
		}
		return new MethodGenerator(this, mv, new MethodInstance(state.getNewType(), access, name, signature, desc));
	}

	public MethodGenerator visitMethod(MethodInstance method)
	{
		Type owner = BytecodeBehaviorState.getState().getNewType();
		method = new MethodInstance(owner, method);
		MethodVisitor mv = super.visitMethod(method.getAccess(), method.getName(), method.getDescriptor(), method.getSignature(), null);
		return new MethodGenerator(this, mv, method);
	}

	public MethodInstance implementSetter(MethodInstance method, FieldInstance field)
	{
		MethodGenerator mg = visitMethod(method);
		mg.putThisField(field, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.loadArg(0);
			}
		});
		mg.returnVoidOrThis();
		mg.endMethod();
		return MethodInstance.findByTemplate(method, false);
	}

	public PropertyInstance implementSetter(PropertyInstance property, FieldInstance field)
	{
		MethodInstance setter = property.getSetter();
		if (setter == null)
		{
			setter = new MethodInstance(getState().getNewType(), Opcodes.ACC_PUBLIC, "set" + property.getName(), property.getSignature(), Type.VOID_TYPE,
					property.getPropertyType());
		}
		implementSetter(setter, field);
		return PropertyInstance.findByTemplate(property, false);
	}

	public MethodInstance implementGetter(MethodInstance method, FieldInstance field)
	{
		MethodGenerator mg = visitMethod(method);
		mg.getThisField(field);
		mg.returnValue();
		mg.endMethod();
		return MethodInstance.findByTemplate(method, false);
	}

	public PropertyInstance implementGetter(PropertyInstance property, FieldInstance field)
	{
		MethodInstance getter = property.getGetter();
		if (getter == null)
		{
			getter = new MethodInstance(getState().getNewType(), Opcodes.ACC_PUBLIC, "get" + property.getName(), property.getSignature(),
					property.getPropertyType());
		}
		implementGetter(getter, field);
		return PropertyInstance.findByTemplate(property, false);
	}

	public PropertyInstance implementLazyInitProperty(PropertyInstance property, Script script, String... fireThisOnPropertyNames)
	{
		FieldInstance field = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "f_" + property.getName(), property.getSignature(),
				property.getPropertyType()));
		MethodGenerator mv = visitMethod(property.getGetter());
		Label returnInstance = mv.newLabel();
		mv.getThisField(field);
		mv.ifNonNull(returnInstance);
		mv.putThisField(field, script);
		mv.mark(returnInstance);
		mv.getThisField(field);
		mv.returnValue();
		mv.endMethod();
		return fireThisOnPropertyChange(property, fireThisOnPropertyNames);
	}

	public PropertyInstance implementProperty(PropertyInstance property, Script getterScript, Script setterScript)
	{
		if (getterScript != null)
		{
			MethodGenerator mv = visitMethod(property.getGetter());
			getterScript.execute(mv);
			mv.endMethod();
		}
		if (setterScript != null)
		{
			MethodGenerator mv = visitMethod(property.getSetter());
			setterScript.execute(mv);
			mv.endMethod();
		}
		return PropertyInstance.findByTemplate(property, false);
	}

	public PropertyInstance fireThisOnPropertyChange(PropertyInstance property, String... propertyNames)
	{
		property = getState().getProperty(property.getName());
		for (String propertyName : propertyNames)
		{
			property.addAnnotation(c_fireThisOPC, propertyName);
		}
		return property;
	}

	public void overrideConstructors(IOverrideConstructorDelegate overrideConstructorDelegate)
	{
		if (getState().getCurrentType().isInterface())
		{
			overrideConstructorDelegate.invoke(this, c_obj);
			return;
		}
		Constructor<?>[] constructors = getState().getCurrentType().getDeclaredConstructors();
		for (Constructor<?> superConstructor : constructors)
		{
			overrideConstructorDelegate.invoke(this, new ConstructorInstance(superConstructor));
		}
	}

	public MethodGenerator startOverrideWithSuperCall(MethodInstance superMethod)
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();

		Type superType = Type.getType(state.getCurrentType());
		if (!superType.equals(superMethod.getOwner()))
		{
			throw new IllegalArgumentException("Not a method of " + state.getCurrentType() + ": " + superMethod);
		}

		MethodInstance overridingMethod = new MethodInstance(state.getNewType(), superMethod);

		MethodGenerator mg = visitMethod(overridingMethod);

		mg.loadThis();
		mg.loadArgs();
		mg.invokeSuper(superMethod);

		return mg;
	}
}
