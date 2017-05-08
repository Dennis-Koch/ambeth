package com.koch.ambeth.training.travelguides.setup;

import com.koch.ambeth.config.IocConfigurationConstants;
import com.koch.ambeth.config.ServiceConfigurationConstants;
import com.koch.ambeth.ioc.ExprModule;
import com.koch.ambeth.ioc.XmlModule;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.RebuildSchema;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.setup.SetupModule;

/**
 * This test is just used for a call to {@link RebuildSchema#main(String[], Class, String)}.
 * 
 * @see com.koch.RebuildSchemaGuideBook.egxp.core.setup.RebuildSchema
 */

@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orms/guidebook-orm.xml"), //
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth", value = "INFO"), //
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.merge.MergeServiceRegistry", value = "info"), //
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.mixin.PropertyChangeMixin", value = "info"), //
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
