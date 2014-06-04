package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.typeinfo.PropertyInfoProviderTest;
import de.osthus.ambeth.typeinfo.PropertyInfoTest;
import de.osthus.ambeth.util.DelegatingConversionHelperTest;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.ioc.AllTests.class, DelegatingConversionHelperTest.class, PropertyInfoProviderTest.class, PropertyInfoTest.class })
public class AllIocTests
{

}