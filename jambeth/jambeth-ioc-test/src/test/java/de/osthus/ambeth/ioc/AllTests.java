package de.osthus.ambeth.ioc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.ioc.annotation.AutowiredTest;
import de.osthus.ambeth.ioc.beanruntime.BeanRuntimeTest;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainerTest;
import de.osthus.ambeth.ioc.extendable.ExtendableBeanTest;
import de.osthus.ambeth.ioc.injection.InjectionTest;
import de.osthus.ambeth.util.ClassTupleExtendableContainerTest;

@RunWith(Suite.class)
@SuiteClasses({ AutowiredTest.class, ServiceContextTest.class, ClassExtendableContainerTest.class, ExtendableBeanTest.class,
		ClassTupleExtendableContainerTest.class, BeanRuntimeTest.class, InjectionTest.class })
public class AllTests
{

}
