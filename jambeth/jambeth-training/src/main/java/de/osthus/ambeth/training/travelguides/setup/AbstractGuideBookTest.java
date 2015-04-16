package de.osthus.ambeth.training.travelguides.setup;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.ExprModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.RebuildSchema;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.util.setup.SetupModule;

/**
 * This test is just used for a call to {@link RebuildSchema#main(String[], Class, String)}.
 * 
 * @see de.osthus.RebuildSchemaGuideBook.egxp.core.setup.RebuildSchema
 */

@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orms/guidebook-orm.xml"), //
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth", value = "INFO"), //
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.merge.MergeServiceRegistry", value = "info"), //
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.mixin.PropertyChangeMixin", value = "info"), //
		@TestProperties(name = IocConfigurationConstants.TrackDeclarationTrace, value = "false"),//
		@TestProperties(name = IocConfigurationConstants.DebugModeActive, value = "false"),
		@TestProperties(name = MergeConfigurationConstants.AlwaysUpdateVersionInChangedEntities, value = "false") })
//
@SQLStructure({ "schema/guidebook.sql" })
@TestFrameworkModule({ TestSetupModule.class, ExprModule.class, XmlModule.class })
@TestModule({ SetupModule.class })
@TestRebuildContext
public class AbstractGuideBookTest extends AbstractInformationBusWithPersistenceTest
{
}
