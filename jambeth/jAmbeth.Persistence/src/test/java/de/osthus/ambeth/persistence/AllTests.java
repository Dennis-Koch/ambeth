package de.osthus.ambeth.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ PersistenceHelperTest.class, FieldTest.class, TableTest.class })
public class AllTests
{

}
