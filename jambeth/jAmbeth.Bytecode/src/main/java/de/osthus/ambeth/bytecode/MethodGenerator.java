package de.osthus.ambeth.bytecode;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.accessor.AccessorTypeProvider;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.compositeid.CompositeIdTypeInfoItem;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.MethodVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.Method;
import de.osthus.ambeth.repackaged.org.objectweb.asm.util.Printer;
import de.osthus.ambeth.repackaged.org.objectweb.asm.util.Textifier;
import de.osthus.ambeth.repackaged.org.objectweb.asm.util.TraceMethodVisitor;
import de.osthus.ambeth.typeinfo.FieldInfoItem;
import de.osthus.ambeth.typeinfo.IEmbeddedTypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class MethodGenerator extends GeneratorAdapter
{
	protected final MethodInstance method;
	protected final ClassGenerator cg;

	protected final Printer methodPrinter = new Textifier();

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, Type owner, int access, String name, String signature, Type returnType, Type... parameters)
	{
		this(cg, mv, new MethodInstance(owner, access, name, signature, returnType, parameters));
	}

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, Type owner, int access, Method method, String signature)
	{
		this(cg, mv, new MethodInstance(owner, access, method, signature));
	}

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, Type owner, java.lang.reflect.Method method)
	{
		this(cg, mv, new MethodInstance(method));
	}

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, MethodInstance method)
	{
		super(Opcodes.ASM4, new TraceMethodVisitor(mv, new Textifier()), method.getAccess(), method.getName(), method.getDescriptor());
		this.cg = cg;
		this.method = method;
	}

	public MethodInstance getMethod()
	{
		return method;
	}

	public ClassGenerator getClassGenerator()
	{
		return cg;
	}

	public int newLocal(Class<?> localVariableType)
	{
		ParamChecker.assertParamNotNull(localVariableType, "localVariableType");
		return newLocal(Type.getType(localVariableType));
	}

	public void newInstance(ConstructorInstance constructor, Script argumentsScript)
	{
		ParamChecker.assertParamNotNull(constructor, "constructor");
		newInstance(constructor.getOwner());
		dup();
		if (argumentsScript != null)
		{
			argumentsScript.execute(this);
		}
		invokeConstructor(constructor);
	}

	public void invokeConstructor(ConstructorInstance constructor)
	{
		ParamChecker.assertParamNotNull(constructor, "constructor");
		invokeSuper(constructor);
	}

	public void invokeSuper(MethodInstance method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		Type currType = BytecodeBehaviorState.getState().getNewType();
		if (currType.equals(method.getOwner()))
		{
			// Given method is not a super method. We look in the existing class hierarchy for a method with the same signature
			if (ConstructorInstance.CONSTRUCTOR_NAME.equals(method.getName()))
			{
				Constructor<?> c_method = ReflectUtil.getDeclaredConstructor(true, BytecodeBehaviorState.getState().getCurrentType(), method.getParameters());

				if (c_method == null)
				{
					throw new IllegalArgumentException("Constructor has no super implementation: " + method);
				}
				method = new ConstructorInstance(c_method);
			}
			else
			{
				java.lang.reflect.Method r_method = ReflectUtil.getDeclaredMethod(true, BytecodeBehaviorState.getState().getCurrentType(), method.getName(),
						method.getParameters());
				if (r_method == null)
				{
					throw new IllegalArgumentException("Method has no super implementation: " + method);
				}
				method = new MethodInstance(r_method);
			}
		}
		invokeOnExactOwner(method);
	}

	public void invokeSuperOfCurrentMethod()
	{
		invokeSuper(getMethod());
	}

	public void invokeOnExactOwner(java.lang.reflect.Method method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getModifiers() & Modifier.STATIC) != 0)
		{
			throw new IllegalArgumentException("Given method is not virtual: " + method);
		}
		invokeSuper(new MethodInstance(method));
	}

	public void invokeOnExactOwner(MethodInstance method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		invokeConstructor(method.getOwner(), method.method);
	}

	public void invokeVirtual(MethodInstance method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getAccess() & Opcodes.ACC_STATIC) != 0)
		{
			throw new IllegalArgumentException("Given method is not virtual: " + method);
		}
		Type owner = method.getOwner();
		if (owner == null)
		{
			owner = BytecodeBehaviorState.getState().getNewType();
		}
		invokeVirtual(owner, method.getMethod());
	}

	public void invokeInterface(MethodInstance method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getAccess() & Opcodes.ACC_STATIC) != 0)
		{
			throw new IllegalArgumentException("Given method is not virtual: " + method);
		}
		Type owner = method.getOwner();
		if (owner == null)
		{
			owner = BytecodeBehaviorState.getState().getNewType();
		}
		invokeInterface(method.getOwner(), method.getMethod());
	}

	public void invokeSuperOf(java.lang.reflect.Method method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		java.lang.reflect.Method superMethod = ReflectUtil.getDeclaredMethod(false, state.getCurrentType(), method.getName(), method.getParameterTypes());
		invokeSuper(new MethodInstance(superMethod));
	}

	public void invokeStatic(java.lang.reflect.Method method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getModifiers() & Modifier.STATIC) == 0)
		{
			throw new IllegalArgumentException("Given method is not static: " + method);
		}
		invokeStatic(Type.getType(method.getDeclaringClass()), Method.getMethod(method));
	}

	public void invokeStatic(MethodInstance method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getAccess() & Opcodes.ACC_STATIC) == 0)
		{
			throw new IllegalArgumentException("Given method is not static: " + method);
		}
		invokeStatic(method.getOwner(), method.getMethod());
	}

	public void callThisGetter(MethodInstance method)
	{
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.access & Opcodes.ACC_STATIC) == 0)
		{
			loadThis();
			invokeVirtual(method);
		}
		else
		{
			invokeStatic(method);
		}
	}

	public void callThisGetter(PropertyInstance property)
	{
		ParamChecker.assertParamNotNull(property, "property");
		callThisGetter(property.getGetter());
	}

	public void callThisSetter(MethodInstance method, Script script)
	{
		ParamChecker.assertParamNotNull(method, "method");
		ParamChecker.assertParamNotNull(script, "script");
		if ((method.access & Opcodes.ACC_STATIC) == 0)
		{
			loadThis();
			script.execute(this);
			invokeVirtual(method);
		}
		else
		{
			script.execute(this);
			invokeStatic(method);
		}
	}

	public void callThisSetter(PropertyInstance property, Script script)
	{
		ParamChecker.assertParamNotNull(property, "property");
		ParamChecker.assertParamNotNull(script, "script");
		callThisSetter(property.getSetter(), script);
	}

	public void getThisField(FieldInstance field)
	{
		ParamChecker.assertParamNotNull(field, "field");
		if ((field.access & Opcodes.ACC_STATIC) == 0)
		{
			loadThis();
			getField(field.getOwner(), field.getName(), field.getType());
		}
		else
		{
			getStatic(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void putThisField(FieldInstance field, Script script)
	{
		ParamChecker.assertParamNotNull(field, "field");
		ParamChecker.assertParamNotNull(script, "script");
		if ((field.access & Opcodes.ACC_STATIC) == 0)
		{
			loadThis();
			script.execute(this);
			putField(field.getOwner(), field.getName(), field.getType());
		}
		else
		{
			script.execute(this);
			putStatic(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void returnVoidOrThis()
	{
		if (!Type.VOID_TYPE.equals(method.getReturnType()))
		{
			loadThis();
		}
		returnValue();
	}

	public void pushNull()
	{
		push((String) null);
	}

	public void pushNullOrZero(Type type)
	{
		switch (type.getSort())
		{
			case Type.LONG:
				push((long) 0);
				break;
			case Type.DOUBLE:
				push((double) 0);
				break;
			case Type.FLOAT:
				push((float) 0);
				break;
			case Type.BOOLEAN:
				push(false);
				break;
			case Type.BYTE:
			case Type.CHAR:
			case Type.SHORT:
			case Type.INT:
				push(0);
				break;
			case Type.ARRAY:
			case Type.OBJECT:
				pushNull();
				break;
			default:
				throw new IllegalArgumentException("Sort not supported: " + type.getSort());
		}
	}

	/**
	 * Generates the instructions to jump to a label based on the comparison of the top two stack values.
	 * 
	 * @param type
	 *            the type of the top two stack values.
	 * @param mode
	 *            how these values must be compared. One of EQ, NE, LT, GE, GT, LE.
	 * @param label
	 *            where to jump if the comparison result is <tt>true</tt>.
	 */
	public void ifCmp(final Class<?> type, final int mode, final Label label)
	{
		ifCmp(Type.getType(type), mode, label);
	}

	public void smartBox(Type unboxedType)
	{
		AccessorTypeProvider.smartBox(this, unboxedType);
	}

	@Override
	public String toString()
	{
		if (!(mv instanceof TraceMethodVisitor))
		{
			return super.toString();
		}
		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);

		((TraceMethodVisitor) mv).p.print(pw);

		return sw.toString();
	}

	public void println(CharSequence text)
	{
		Type type = Type.getType(PrintStream.class);
		getStatic(Type.getType(System.class), "out", type);
		MethodInstance m_println = new MethodInstance(type, PrintStream.class, "println", String.class);
		push(text.toString());
		invokeVirtual(m_println);
	}

	public void popIfReturnValue(MethodInstance method)
	{
		if (Type.VOID_TYPE.equals(method.getReturnType()))
		{
			return;
		}
		pop();
	}

	public void push(Class<?> type)
	{
		push(type != null ? Type.getType(type) : null);
	}

	public <V extends Enum<?>> void pushEnum(V enumInstance)
	{
		ParamChecker.assertParamNotNull(enumInstance, "enumInstance");
		ParamChecker.assertTrue(enumInstance.getClass().isEnum(), "enumInstance");
		Class<?> owner = enumInstance.getClass();
		String fieldName = enumInstance.name();
		getThisField(new FieldInstance(ReflectUtil.getDeclaredField(owner, fieldName)));
	}

	public void tryFinally(Script tryScript, Script finallyScript)
	{
		Label tryLabel = newLabel();
		Label catchLabel = newLabel();
		Label successLabel = newLabel();

		visitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);

		mark(tryLabel);

		tryScript.execute(this);
		goTo(successLabel);

		mark(catchLabel);
		int loc_throwable = newLocal(Throwable.class);
		storeLocal(loc_throwable);

		finallyScript.execute(this);

		loadLocal(loc_throwable);
		throwException();

		mark(successLabel);
		finallyScript.execute(this);
	}

	public void invokeGetValue(ITypeInfoItem member, final Script thisScript)
	{
		if (member instanceof PropertyInfoItem)
		{
			java.lang.reflect.Method getter = ((MethodPropertyInfo) ((PropertyInfoItem) member).getProperty()).getGetter();

			MethodInstance m_getter = new MethodInstance(getter);

			if (thisScript != null)
			{
				thisScript.execute(this);
			}
			if (getter.getDeclaringClass().isInterface())
			{
				invokeInterface(m_getter);
			}
			else
			{
				invokeVirtual(m_getter);
			}
		}
		else if (member instanceof CompositeIdTypeInfoItem)
		{
			final CompositeIdTypeInfoItem cidMember = (CompositeIdTypeInfoItem) member;

			ConstructorInstance c_compositeId = new ConstructorInstance(cidMember.getRealTypeConstructorAccess());
			newInstance(c_compositeId, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					ITypeInfoItem[] members = cidMember.getMembers();
					for (int a = 0, size = members.length; a < size; a++)
					{
						invokeGetValue(members[a], thisScript);
					}
				}
			});
		}
		else if (member instanceof IEmbeddedTypeInfoItem)
		{
			IEmbeddedTypeInfoItem embedded = (IEmbeddedTypeInfoItem) member;
			ITypeInfoItem[] memberPath = embedded.getMemberPath();
			invokeGetValue(memberPath[0], thisScript);
			for (int a = 1, size = memberPath.length; a < size; a++)
			{
				invokeGetValue(memberPath[a], null);
			}
			invokeGetValue(embedded.getChildMember(), null);
		}
		else
		{
			FieldInstance field = new FieldInstance(((FieldInfoItem) member).getField());

			if (thisScript != null)
			{
				thisScript.execute(this);
			}
			getField(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void checkCast(Class<?> type)
	{
		checkCast(Type.getType(type));
	}

	@Override
	public void endMethod()
	{
		try
		{
			super.endMethod();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while finishing method: " + getMethod() + "\n" + toString());
		}
	}
}
