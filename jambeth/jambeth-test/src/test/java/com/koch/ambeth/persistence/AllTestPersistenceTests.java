package com.koch.ambeth.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.persistence.noversion.NoVersionTest.class, //
		com.koch.ambeth.persistence.schema.JenkinsSingleRandomUserTest.class, //
		com.koch.ambeth.persistence.schema.MultiSchemaTest.class, //
		com.koch.ambeth.persistence.schema.RandomUserTest.class, //
		com.koch.ambeth.persistence.schema.SingleRandomUserTest.class, //
		com.koch.ambeth.persistence.validation.PropertyMappingValidationTest.class, //
		com.koch.ambeth.persistence.jdbc.bigstatements.BigStatementTest.class,//
		com.koch.ambeth.persistence.jdbc.setup.TestSetupTest.class, //
		com.koch.ambeth.persistence.find.FinderTest.class })
public class AllTestPersistenceTests
{
}
