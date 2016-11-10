package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.util.ReflectUtil;

public class DelegateVisitor extends ClassGenerator
{
	private final IMap<Method, Method> mappedMethods;

	private final Class<?> type;

	public DelegateVisitor(ClassVisitor cv, Class<?> type, IMap<Method, Method> mappedMethods)
	{
		super(cv);
		this.type = type;
		this.mappedMethods = mappedMethods;
	}

	@Override
	public void visitEnd()
	{
		LinkedHashMap<MethodInstance, MethodInstance> methodsAsKey = new LinkedHashMap<MethodInstance, MethodInstance>();
		for (Method method : ReflectUtil.getDeclaredMethodsInHierarchy(type))
		{
			if (Modifier.isStatic(method.getModifiers()) || Modifier.isFinal(method.getModifiers()))
			{
				continue;
			}
			MethodInstance mi = new MethodInstance(method);
			methodsAsKey.put(mi.deriveOwner(), mi);
		}
		final FieldInstance f_target = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$target", null, type));

		{
			MethodGenerator mg = visitMethod(new ConstructorInstance(Opcodes.ACC_PUBLIC, null, Object.class));
			mg.loadThis();
			try
			{
				mg.invokeOnExactOwner(new ConstructorInstance(Object.class.getConstructor()));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			mg.putThisField(f_target, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.loadArg(0);
					mg.checkCast(f_target.getType());
				}
			});
			mg.returnValue();
			mg.endMethod();
		}
		for (Entry<Method, Method> entry : mappedMethods)
		{
			Method method = entry.getKey();
			Method targetMethod = entry.getValue();

			MethodInstance mi = new MethodInstance(method);
			methodsAsKey.remove(mi.deriveOwner());

			MethodGenerator mv = visitMethod(mi);
			mv.loadThis();
			mv.getField(f_target);
			Class<?>[] sourceParameterTypes = method.getParameterTypes();
			Class<?>[] targetParameterTypes = targetMethod.getParameterTypes();
			for (int a = 0, size = targetParameterTypes.length; a < size; a++)
			{
				mv.loadArg(a);
				if (sourceParameterTypes[a] != targetParameterTypes[a])
				{
					mv.checkCast(targetParameterTypes[a]);
				}
			}
			mv.invokeVirtual(new MethodInstance(targetMethod));
			mv.returnValue();
			mv.endMethod();
		}
		for (Entry<MethodInstance, MethodInstance> entry : methodsAsKey)
		{
			MethodGenerator mv = visitMethod(entry.getKey());
			mv.loadThis();
			mv.getField(f_target);
			mv.loadArgs();
			mv.invokeVirtual(entry.getValue());
			mv.returnValue();
			mv.endMethod();
		}
		super.visitEnd();
	}
}
