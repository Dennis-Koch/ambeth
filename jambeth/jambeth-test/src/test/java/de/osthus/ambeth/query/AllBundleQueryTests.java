package de.osthus.ambeth.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FilterDescriptorTest.class, //
		FilterToQueryBuilderTest.class, //
		PropertyQueryTest.class, //
		Query10000Test.class, //
		QueryMassdataTest.class, //
		QueryTest.class,//
		StoredFunctionTest.class, //
		de.osthus.ambeth.query.alternateid.MultiAlternateIdQueryTest.class, //
		de.osthus.ambeth.query.backwards.BackwardsQueryTest.class, //
		de.osthus.ambeth.query.behavior.QueryBehaviorTest.class, //
		de.osthus.ambeth.query.isin.QueryIsInMassdataTest.class, //
		de.osthus.ambeth.query.sql.SqlQueryTest.class, //
		de.osthus.ambeth.query.subquery.SubQueryTest.class })
public class AllBundleQueryTests
{
	// Intended blank
}
