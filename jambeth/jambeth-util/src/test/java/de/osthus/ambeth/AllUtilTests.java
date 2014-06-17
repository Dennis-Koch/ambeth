package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.collections.IntKeyMapTest.class, //
		de.osthus.ambeth.collections.InterfaceFastListTest.class, //
		de.osthus.ambeth.reflectasm.ReflectAsmPerformanceTest.class, //
		de.osthus.ambeth.threading.FastThreadPoolTest.class, //
		de.osthus.ambeth.util.ListUtilTest.class, //
		de.osthus.ambeth.util.converter.AllTests.class })
public class AllUtilTests
{
}
