package de.osthus.ambeth.testutil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.testutil.contextstore.ServiceContextStoreTest;

@RunWith(Suite.class)
@SuiteClasses({ ServiceContextStoreTest.class })
public class AllTestUtilTests
{
}
