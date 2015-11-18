package de.osthus.ambeth.cache.cacheretriever;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = CacheRetrieverRegistry20Test.basePath + "orm_2.0.xml;"
		+ CacheRetrieverRegistry20Test.basePath + "external-orm_2.0.xml")
public class CacheRetrieverRegistry20Test extends CacheRetrieverRegistryTest
{
}
