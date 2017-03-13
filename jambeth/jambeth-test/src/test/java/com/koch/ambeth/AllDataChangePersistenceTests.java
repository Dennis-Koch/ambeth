package com.koch.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.services.DataChangeEventDAOTest;
import com.koch.ambeth.services.DataChangeEventServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ DataChangeEventDAOTest.class, DataChangeEventServiceTest.class })
public class AllDataChangePersistenceTests
{
}
