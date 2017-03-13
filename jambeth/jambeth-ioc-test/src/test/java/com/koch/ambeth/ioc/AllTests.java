package com.koch.ambeth.ioc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.ioc.annotation.AutowiredTest;
import com.koch.ambeth.ioc.beanruntime.BeanRuntimeTest;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainerTest;
import com.koch.ambeth.ioc.extendable.ExtendableBeanTest;
import com.koch.ambeth.ioc.injection.InjectionTest;
import com.koch.ambeth.util.ClassTupleExtendableContainerTest;

@RunWith(Suite.class)
@SuiteClasses({ AutowiredTest.class, ServiceContextTest.class, ClassExtendableContainerTest.class, ExtendableBeanTest.class,
		ClassTupleExtendableContainerTest.class, BeanRuntimeTest.class, InjectionTest.class })
public class AllTests
{

}
