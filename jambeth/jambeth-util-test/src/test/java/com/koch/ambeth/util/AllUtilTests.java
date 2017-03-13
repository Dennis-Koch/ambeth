package com.koch.ambeth.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.util.collections.IntKeyMapTest.class, //
		com.koch.ambeth.util.collections.InterfaceFastListTest.class, //
		com.koch.ambeth.util.reflectasm.ReflectAsmPerformanceTest.class, //
		com.koch.ambeth.util.threading.FastThreadPoolTest.class, //
		com.koch.ambeth.util.util.ListUtilTest.class, //
		com.koch.ambeth.util.util.converter.AllTests.class })
public class AllUtilTests
{
}
