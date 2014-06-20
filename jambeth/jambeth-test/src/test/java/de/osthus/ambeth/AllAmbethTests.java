package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.cache.AllBundleCacheTests.class, //
		de.osthus.ambeth.AllCacheServerTests.class, //
		de.osthus.ambeth.AllDataChangePersistenceTests.class, //
		de.osthus.ambeth.AllIocTests.class,//
		de.osthus.ambeth.AllMergeBytecodeTests.class, //
		de.osthus.ambeth.AllPersistenceTests.class, //
		de.osthus.ambeth.AllMergeTests.class, //
		de.osthus.ambeth.AllUtilTests.class, //
		de.osthus.ambeth.bytecode.AllBundleBytecodeTests.class, //
		de.osthus.ambeth.merge.mergecontroller.AllMergeControllerTests.class, //
		de.osthus.ambeth.merge.orihelper.AllORIHelperTests.class, //
		de.osthus.ambeth.orm20.AllOrm20Tests.class, //
		de.osthus.ambeth.persistence.AllTestPersistenceTests.class, //
		de.osthus.ambeth.persistence.jdbc.AllTests.class, //
		de.osthus.ambeth.persistence.streaming.StreamingEntityTest.class, //
		de.osthus.ambeth.persistence.xml.AllPersistenceXmlTests.class, //
		de.osthus.ambeth.query.AllBundleQueryTests.class, //
		de.osthus.ambeth.query.AllQueryTests.class, //
		de.osthus.ambeth.relations.AllRelationTests.class,//
		de.osthus.ambeth.service.AllServiceTests.class, //
		de.osthus.ambeth.testutil.AllTestUtilTests.class, //
		de.osthus.ambeth.testutil.AllTestUtilPersistenceTests.class, //
		de.osthus.ambeth.xml.AllXmlTests.class, //
		de.osthus.ambeth.xml.oriwrapper.AllOriWrapperTests.class })
public class AllAmbethTests
{
}
