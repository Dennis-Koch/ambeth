package de.osthus.ambeth.bytecode.behavior;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassReader;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.tree.ClassNode;

public abstract class AbstractBehavior implements IBytecodeBehavior
{
	@Autowired
	protected IServiceContext beanContext;

	protected Type getDeclaringType(Member member, Type newEntityType)
	{
		if (member.getDeclaringClass().isInterface())
		{
			return newEntityType;
		}
		return Type.getType(member.getDeclaringClass());
	}

	protected ClassNode readClassNode(Class<?> type)
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return readClassNode(classLoader.getResourceAsStream(Type.getInternalName(type) + ".class"));
	}

	protected ClassNode readClassNode(InputStream is)
	{
		try
		{
			ClassReader cr = new ClassReader(is);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			return cn;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				// Intended blank
			}
		}
	}

	protected boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotationType)
	{
		if (type == null)
		{
			return false;
		}
		if (isAnnotationPresentIntern(type, annotationType))
		{
			return true;
		}
		Class<?>[] interfaces = type.getInterfaces();
		for (Class<?> interfaceType : interfaces)
		{
			if (isAnnotationPresent(interfaceType, annotationType))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean isAnnotationPresentIntern(Class<?> type, Class<? extends Annotation> annotationType)
	{
		if (type == null)
		{
			return false;
		}
		if (type.isAnnotationPresent(annotationType))
		{
			return true;
		}
		return isAnnotationPresentIntern(type.getSuperclass(), annotationType);
	}

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[0];
	}

	@Override
	public Class<?> getTypeToExtendFrom(Class<?> originalType, Class<?> currentType, IEnhancementHint hint)
	{
		return null;
	}
}
