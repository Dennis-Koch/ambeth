package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.services.DataChangeEventDAOTest;
import de.osthus.ambeth.services.DataChangeEventServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ DataChangeEventDAOTest.class, DataChangeEventServiceTest.class })
public class AllDataChangePersistenceTests
{
}
