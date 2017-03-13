package com.koch.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.persistence.AllTests.class, com.koch.ambeth.persistence.sql.AllTests.class, com.koch.ambeth.orm20.AllTests.class })
public class AllPersistenceTests
{
}
