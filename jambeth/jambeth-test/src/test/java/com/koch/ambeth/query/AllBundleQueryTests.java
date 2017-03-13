package com.koch.ambeth.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FilterDescriptorTest.class, //
		FilterToQueryBuilderTest.class, //
		PropertyQueryTest.class, //
		Query10000Test.class, //
		// QueryMassdataTest.class, //
		QueryTest.class,//
		StoredFunctionTest.class, //
		com.koch.ambeth.query.alternateid.MultiAlternateIdQueryTest.class, //
		com.koch.ambeth.query.backwards.BackwardsQueryTest.class, //
		com.koch.ambeth.query.behavior.QueryBehaviorTest.class, //
		// com.koch.ambeth.query.isin.QueryIsInMassdataTest.class, //
		com.koch.ambeth.query.subquery.SubQueryTest.class })
public class AllBundleQueryTests
{
	// Intended blank
}
