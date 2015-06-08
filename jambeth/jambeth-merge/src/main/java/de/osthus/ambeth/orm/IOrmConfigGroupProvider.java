package de.osthus.ambeth.orm;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IOrmConfigGroupProvider
{

	IOrmConfigGroup getOrmConfigGroup(String xmlFileNames);

}