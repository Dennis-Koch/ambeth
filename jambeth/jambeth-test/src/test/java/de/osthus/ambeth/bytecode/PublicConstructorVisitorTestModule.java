package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import de.osthus.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import de.osthus.ambeth.bytecode.abstractobject.ImplementAbstractObjectFactory;
import de.osthus.ambeth.bytecode.behavior.ImplementAbstractObjectBehavior;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

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
