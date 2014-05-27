package de.osthus.ambeth.query;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.query.alternateid.MultiAlternateIdQueryTest;
import de.osthus.ambeth.query.behavior.QueryBehaviorTest;

@RunWith(Suite.class)
@SuiteClasses({ FilterDescriptorTest.class, MultiAlternateIdQueryTest.class, QueryBehaviorTest.class, QueryTest.class,
		de.osthus.ambeth.query.sql.SqlQueryTest.class, Query10000Test.class, PropertyQueryTest.class, StoredFunctionTest.class,
		de.osthus.ambeth.query.subquery.SubQueryTest.class, de.osthus.ambeth.query.backwards.BackwardsQueryTest.class })
public class AllBundleQueryTests
{
	// Intended blank
}
