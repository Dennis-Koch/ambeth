package com.koch.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.ioc.link.LinkContainerTest;
import com.koch.ambeth.ioc.performance.IocPerformanceTest;
import com.koch.ambeth.testutil.AmbethIocRunnerTest;
import com.koch.ambeth.typeinfo.PropertyInfoProviderTest;
import com.koch.ambeth.typeinfo.PropertyInfoTest;
import com.koch.ambeth.util.ClassTupleExtendableContainerPerformanceTest;
import com.koch.ambeth.util.DelegatingConversionHelperTest;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.ioc.AllTests.class, AmbethIocRunnerTest.class, ClassTupleExtendableContainerPerformanceTest.class, LinkContainerTest.class,
		IocPerformanceTest.class, DelegatingConversionHelperTest.class, PropertyInfoProviderTest.class, PropertyInfoTest.class })
public class AllIocTests
{
}
