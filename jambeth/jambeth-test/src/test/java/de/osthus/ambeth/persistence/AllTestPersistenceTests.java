package de.osthus.ambeth.persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.persistence.noversion.NoVersionTest.class, //
		de.osthus.ambeth.persistence.schema.JenkinsSingleRandomUserTest.class, //
		de.osthus.ambeth.persistence.schema.MultiSchemaTest.class, //
		de.osthus.ambeth.persistence.schema.RandomUserTest.class, //
		de.osthus.ambeth.persistence.schema.SingleRandomUserTest.class, //
		de.osthus.ambeth.persistence.validation.PropertyMappingValidationTest.class, //
		de.osthus.ambeth.persistence.jdbc.setup.TestSetupTest.class, //
		de.osthus.ambeth.persistence.find.FinderTest.class })
public class AllTestPersistenceTests
{
}
