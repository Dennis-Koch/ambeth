package com.koch.ambeth.bytecode;

import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import com.koch.ambeth.bytecode.behavior.ImplementAbstractObjectBehavior;
import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.bytecode.abstractobject.ImplementAbstractObjectFactory;

public class PublicConstructorVisitorTestModule implements IInitializingModule
{
	private static final String IMPLEMENT_ABSTRACT_OBJECT_FACTORY = "implementAbstractObjectFactory";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// creates objects that implement the interfaces
		beanContextFactory.registerBean(IMPLEMENT_ABSTRACT_OBJECT_FACTORY, ImplementAbstractObjectFactory.class).autowireable(
				IImplementAbstractObjectFactory.class, IImplementAbstractObjectFactoryExtendable.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ImplementAbstractObjectBehavior.class).propertyRef("ImplementAbstractObjectFactory",
				IMPLEMENT_ABSTRACT_OBJECT_FACTORY);
	}
};
