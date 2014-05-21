package de.osthus.ambeth.testutil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.testutil.persistencerunner.AllPersistenceRunnerTests;

@RunWith(Suite.class)
@SuiteClasses({ AllPersistenceRunnerTests.class })
public class AllTestUtilPersistenceTests
{
}
