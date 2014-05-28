package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.merge.mergecontroller.AllMergeControllerTests;
import de.osthus.ambeth.persistence.streaming.StreamingEntityTest;
import de.osthus.ambeth.persistence.xml.AllPersistenceXmlTests;
import de.osthus.ambeth.service.AllServiceTests;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.cache.AllBundleCacheTests.class, de.osthus.ambeth.AllCacheServerTests.class,
		de.osthus.ambeth.AllDataChangePersistenceTests.class, de.osthus.ambeth.AllIocTests.class, AllMergeControllerTests.class,
		de.osthus.ambeth.AllMergeBytecodeTests.class, de.osthus.ambeth.merge.orihelper.AllORIHelperTests.class, de.osthus.ambeth.AllPersistenceTests.class,
		de.osthus.ambeth.persistence.jdbc.AllTests.class, de.osthus.ambeth.AllMergeTests.class, AllServiceTests.class, AllPersistenceXmlTests.class,
		de.osthus.ambeth.orm20.AllOrm20Tests.class, de.osthus.ambeth.query.AllBundleQueryTests.class, de.osthus.ambeth.query.AllQueryTests.class,
		de.osthus.ambeth.testutil.AllTestUtilTests.class, de.osthus.ambeth.testutil.AllTestUtilPersistenceTests.class, de.osthus.ambeth.AllUtilTests.class,
		de.osthus.ambeth.xml.AllXmlTests.class, de.osthus.ambeth.xml.oriwrapper.AllOriWrapperTests.class, StreamingEntityTest.class })
public class AllAmbethTests
{
}
