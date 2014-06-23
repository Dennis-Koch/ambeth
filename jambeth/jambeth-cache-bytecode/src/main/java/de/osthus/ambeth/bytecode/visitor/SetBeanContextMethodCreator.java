package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public class SetBeanContextMethodCreator extends ClassGenerator
{
	private static final String beanContextName = "$beanContext";

	public static PropertyInstance getBeanContextPI(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(IServiceContext.class);
		PropertyInstance pi = getState().getProperty(beanContextName, bean.getClass());
		if (pi != null)
		{
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(beanContextName, bean);
	}

	public SetBeanContextMethodCreator(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		// force implementation
		getBeanContextPI(this);

		super.visitEnd();
	}
}
