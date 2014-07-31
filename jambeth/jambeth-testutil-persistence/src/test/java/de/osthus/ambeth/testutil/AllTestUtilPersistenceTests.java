package de.osthus.ambeth.testutil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.testutil.persistencerunner.AllPersistenceRunnerTests;
import de.osthus.ambeth.testutil.resource.ResourceTest;

@RunWith(Suite.class)
@SuiteClasses({ ResourceTest.class, AllPersistenceRunnerTests.class })
public class AllTestUtilPersistenceTests
{
}
