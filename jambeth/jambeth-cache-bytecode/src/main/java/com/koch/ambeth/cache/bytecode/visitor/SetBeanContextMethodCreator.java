package com.koch.ambeth.cache.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.ioc.IServiceContext;

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
