package de.osthus.ambeth.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.transfer.ServiceDescriptionSerializationTest;
import de.osthus.ambeth.transfer.ServiceResultDeSerializationTest;

@RunWith(Suite.class)
@SuiteClasses({ MergeServiceTest.class, ProcessServiceTest.class, ServiceDescriptionSerializationTest.class, ServiceResultDeSerializationTest.class })
public class AllServiceTests
{
}
