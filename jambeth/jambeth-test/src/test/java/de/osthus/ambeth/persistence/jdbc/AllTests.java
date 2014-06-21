package de.osthus.ambeth.persistence.jdbc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.cache.RootCacheInvalidationTest;
import de.osthus.ambeth.cache.SecondLevelCacheTest;
import de.osthus.ambeth.cache.cachetype.CacheTypeTest;
import de.osthus.ambeth.persistence.event.MultiEventTest;
import de.osthus.ambeth.persistence.external.compositeid.CompositeIdExternalEntityTest;
import de.osthus.ambeth.persistence.jdbc.alternateid.AlternateIdTest;
import de.osthus.ambeth.persistence.jdbc.array.ArrayTest;
import de.osthus.ambeth.persistence.jdbc.auto.AutoIndexTest;
import de.osthus.ambeth.persistence.jdbc.compositeid.CompositeIdTest;
import de.osthus.ambeth.persistence.jdbc.ignoretable.IgnoreTableTest;
import de.osthus.ambeth.persistence.jdbc.interf.InterfaceEntityTest;
import de.osthus.ambeth.persistence.jdbc.lob.BlobTest;
import de.osthus.ambeth.persistence.jdbc.lob.ClobTest;
import de.osthus.ambeth.persistence.jdbc.mapping.MapperPerformanceTest;
import de.osthus.ambeth.persistence.jdbc.mapping.MapperTest;
import de.osthus.ambeth.persistence.jdbc.mapping.MasterDetailTest;
import de.osthus.ambeth.persistence.jdbc.splitloading.SplitLoadingTest;
import de.osthus.ambeth.persistence.jdbc.synonym.SynonymTest;
import de.osthus.ambeth.persistence.update.RelationUpdateTest;
import de.osthus.ambeth.persistence.xml.RelationAutomappingTest;
import de.osthus.ambeth.persistence.xml.Relations20Test;
import de.osthus.ambeth.persistence.xml.RelationsTest;

@RunWith(Suite.class)
@SuiteClasses({ AlternateIdTest.class, ArrayTest.class, AutoIndexTest.class, BlobTest.class, CacheTypeTest.class, ClobTest.class, CompositeIdTest.class,
		ConnectionTest.class, ConnectionHandlingTest.class, CompositeIdExternalEntityTest.class, IgnoreTableTest.class, InterfaceEntityTest.class,
		JDBCDatabaseTest.class, MapperTest.class, MapperPerformanceTest.class, MasterDetailTest.class, MultiEventTest.class, OptimisticLockTest.class,
		RelationsTest.class, RelationAutomappingTest.class, RelationUpdateTest.class, Relations20Test.class, RootCacheInvalidationTest.class,
		SecondLevelCacheTest.class, SplitLoadingTest.class, SqlInjectionTest.class, SynonymTest.class })
public class AllTests
{
}
