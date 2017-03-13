package com.koch.ambeth.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.merge.server.service.MergeServiceTest;
import com.koch.ambeth.service.name.ProcessServiceNamedTest;
import com.koch.ambeth.transfer.ServiceDescriptionSerializationTest;
import com.koch.ambeth.transfer.ServiceResultDeSerializationTest;

@RunWith(Suite.class)
@SuiteClasses({ MergeServiceTest.class, ProcessServiceTest.class, ProcessServiceNamedTest.class, ServiceDescriptionSerializationTest.class,
		ServiceResultDeSerializationTest.class })
public class AllServiceTests
{
}
