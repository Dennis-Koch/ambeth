package com.koch.ambeth;

import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.factory.PropertiesPropertySource;
import com.koch.ambeth.ioc.spring.AmbethBootstrapSpringConfig;
import com.koch.ambeth.ioc.spring.SpringInitializingModule;
import com.koch.ambeth.ioc.spring.SpringInitializingModuleFinalizer;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.StateRollback;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;

public class TestSpring {
    public static void main(String[] args) {
        var properties = new Properties(Properties.getApplication());
        properties.fillWithCommandLineArgs(args);

        ClassLoader classLoader = null;
        if (classLoader != null) {
            properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);
        } else {
            classLoader = properties.get(IocConfigurationConstants.ExplicitClassLoader);
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
                properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);
            }
        }

        var ctx = new AnnotationConfigApplicationContext();
        var env = new StandardEnvironment();
        var propertySources = env.getPropertySources();
        propertySources.addFirst(new PropertiesPropertySource("ambeth-props", properties));
        ctx.setEnvironment(env);
        ctx.register(AmbethBootstrapSpringConfig.class);

        var bundleModules = new InformationBus().getBundleModules();
        var iocModule = Arrays.stream(bundleModules).filter(IocModule.class::isAssignableFrom).findFirst().orElse(null);
        ctx.registerBean(iocModule.getSimpleName(), SpringInitializingModule.class, () -> new SpringInitializingModule(iocModule), bd -> bd.setLazyInit(false));
        for (var bundleModule : bundleModules) {
            if (iocModule == bundleModule) {
                // module already processed first
                continue;
            }
            ctx.registerBean(bundleModule.getSimpleName(), SpringInitializingModule.class, () -> new SpringInitializingModule(bundleModule), bd -> bd.setLazyInit(false));
        }
        ctx.registerBean(SpringInitializingModuleFinalizer.class);

        var rollback = StateRollback.chain(chain -> {
            chain.append(AmbethBootstrapSpringConfig.pushBeanFactoryPostProcessor(factory -> {
                factory.registerSingleton(AmbethBootstrapSpringConfig.PROPERTIES_BEAN_NAME, properties);
                return properties;
            }));
            chain.append(AmbethBootstrapSpringConfig.pushApplicationContext(ctx));
        });
        try {
            ctx.refresh();

            var tlObjectCollector = ctx.getBean(IThreadLocalObjectCollector.class);
            var sb = tlObjectCollector.create(StringBuilder.class);
            try {

            } finally {
                tlObjectCollector.dispose(sb);
            }

            var cache = ctx.getBean(ICache.class);
            cache.getObject(Material.class, 1);

        } finally {
            rollback.rollback();
            ctx.close();
        }
    }
}
