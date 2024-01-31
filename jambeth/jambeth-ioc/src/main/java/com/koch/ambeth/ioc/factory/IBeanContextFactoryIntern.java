package com.koch.ambeth.ioc.factory;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.config.IProperties;

import java.util.List;

public interface IBeanContextFactoryIntern extends IBeanContextFactory {
    List<IBeanConfiguration> getBeanConfigurations();

    IBeanConfiguration getBeanConfiguration(String beanName);

    ILinkedMap<String, List<String>> getBeanNameToAliasesMap();

    Properties getProperties();
}
