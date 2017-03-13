package com.koch.ambeth.cache.cacheretriever;

import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = CacheRetrieverRegistry20Test.basePath + "orm_2.0.xml;"
		+ CacheRetrieverRegistry20Test.basePath + "external-orm_2.0.xml")
public class CacheRetrieverRegistry20Test extends CacheRetrieverRegistryTest
{
}
