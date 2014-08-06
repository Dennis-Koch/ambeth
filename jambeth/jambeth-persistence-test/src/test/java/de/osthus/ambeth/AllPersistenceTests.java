package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.persistence.AllTests.class, de.osthus.ambeth.sql.AllTests.class, de.osthus.ambeth.orm20.AllTests.class })
public class AllPersistenceTests
{
}
