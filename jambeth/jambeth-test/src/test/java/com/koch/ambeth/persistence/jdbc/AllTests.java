package com.koch.ambeth.persistence.jdbc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.cache.RootCacheInvalidationTest;
import com.koch.ambeth.cache.SecondLevelCacheTest;
import com.koch.ambeth.cache.cachetype.CacheTypeTest;
import com.koch.ambeth.persistence.event.MultiEventTest;
import com.koch.ambeth.persistence.external.compositeid.CompositeIdExternalEntityTest;
import com.koch.ambeth.persistence.jdbc.alternateid.AlternateIdTest;
import com.koch.ambeth.persistence.jdbc.array.ArrayTest;
import com.koch.ambeth.persistence.jdbc.auto.AutoIndexFalseTest;
import com.koch.ambeth.persistence.jdbc.auto.AutoIndexTrueTest;
import com.koch.ambeth.persistence.jdbc.compositeid.CompositeIdTest;
import com.koch.ambeth.persistence.jdbc.ignoretable.IgnoreTableTest;
import com.koch.ambeth.persistence.jdbc.interf.InterfaceEntityTest;
import com.koch.ambeth.persistence.jdbc.lob.BlobTest;
import com.koch.ambeth.persistence.jdbc.lob.ClobTest;
import com.koch.ambeth.persistence.jdbc.mapping.MapperPerformanceTest;
import com.koch.ambeth.persistence.jdbc.mapping.MapperTest;
import com.koch.ambeth.persistence.jdbc.mapping.MasterDetailTest;
import com.koch.ambeth.persistence.jdbc.splitloading.SplitLoadingTest;
import com.koch.ambeth.persistence.jdbc.synonym.SynonymTest;
import com.koch.ambeth.persistence.update.RelationUpdateTest;
import com.koch.ambeth.persistence.xml.RelationAutomappingTest;
import com.koch.ambeth.persistence.xml.Relations20Test;
import com.koch.ambeth.persistence.xml.RelationsTest;

@RunWith(Suite.class)
@SuiteClasses({ AlternateIdTest.class, ArrayTest.class, AutoIndexTrueTest.class, AutoIndexFalseTest.class, BlobTest.class, CacheTypeTest.class, ClobTest.class, CompositeIdTest.class,
		ConnectionTest.class, ConnectionHandlingTest.class, CompositeIdExternalEntityTest.class, IgnoreTableTest.class, InterfaceEntityTest.class,
		JDBCDatabaseTest.class, MapperTest.class, MapperPerformanceTest.class, MasterDetailTest.class, MultiEventTest.class, OptimisticLockTest.class,
		RelationsTest.class, RelationAutomappingTest.class, RelationUpdateTest.class, Relations20Test.class, RootCacheInvalidationTest.class,
		SecondLevelCacheTest.class, SplitLoadingTest.class, SqlInjectionTest.class, SynonymTest.class })
public class AllTests
{
}
