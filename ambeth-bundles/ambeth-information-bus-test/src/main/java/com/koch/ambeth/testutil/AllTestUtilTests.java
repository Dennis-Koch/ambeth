package com.koch.ambeth.testutil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.testutil.contextstore.ServiceContextStoreTest;

@RunWith(Suite.class)
@SuiteClasses({ ServiceContextStoreTest.class, TestContextTest.class })
public class AllTestUtilTests
{
}
