package com.koch.ambeth.informationbus.testutil.contextstore;

/*-
 * #%L
 * jambeth-information-bus
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.lang.reflect.Method;
import java.util.List;

public class ServiceContextStore implements IServiceContextStore, IServiceContextStoreConf {
    private final IMap<String, IServiceContext> nameToServiceContext = new HashMap<>();

    private final List<IInterconnectConfig> interconnectConfigs = new ArrayList<>();

    private final List<IInjectionConfig> injectionConfigs = new ArrayList<>();

    @Override
    public IServiceContextStoreConf addContext(String name, IServiceContext context) {
        ParamChecker.assertNotNull(name, "name");
        ParamChecker.assertFalse(name.isEmpty(), "name");
        ParamChecker.assertNotNull(context, "context");

        if (!nameToServiceContext.putIfNotExists(name, context)) {
            throw new IllegalArgumentException("Already a context with name '" + name + "' in store");
        }

        return this;
    }

    @Override
    public IServiceContext getContext(String contextName) {
        return nameToServiceContext.get(contextName);
    }

    @Override
    public IServiceContextStoreConf withConfig(IInterconnectConfig config) {
        ParamChecker.assertNotNull(config, "config");
        interconnectConfigs.add(config);

        return this;
    }

    @Override
    public IServiceContextStoreConf withConfig(Class<? extends IInterconnectConfig> configType) {
        try {
            IInterconnectConfig config = configType.newInstance();
            withConfig(config);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }

        return this;
    }

    @Override
    public IServiceContextStore finish() {
        for (IInterconnectConfig config : interconnectConfigs) {
            config.interconnect(this);
        }
        interconnectConfigs.clear();

        for (IInjectionConfig config : injectionConfigs) {
            config.validate();
            IBeanGetter sourceBean = config.getSourceBeanGetter();
            IBeanGetter targetBean = config.getTargetBeanGetter();
            String propertyName = config.getTargetPropertyName();
            interconnect(sourceBean, targetBean, propertyName);
        }
        injectionConfigs.clear();

        return this;
    }

    @Override
    public IInjectNeedBean injectFrom(String contextName) {
        InjectionConfig injectionConfig = new InjectionConfig();
        injectionConfig.setSourceContextName(contextName);
        injectionConfigs.add(injectionConfig);

        return injectionConfig;
    }

    @Override
    public IInjectNeedTargetContext injectBean(IBeanGetter beanGetter) {
        InjectionConfig injectionConfig = new InjectionConfig();
        injectionConfig.setSourceBeanGetter(beanGetter);
        injectionConfigs.add(injectionConfig);

        return injectionConfig;
    }

    @Override
    public IInjectNeedTargetContext inject(Object bean) {
        ParamChecker.assertNotNull(bean, "bean");
        DirectBeanGetter beanGetter = new DirectBeanGetter();
        beanGetter.setBean(bean);
        IInjectNeedTargetContext injectionConfig = injectBean(beanGetter);

        return injectionConfig;
    }

    private void interconnect(IBeanGetter sourceBeanGetter, IBeanGetter targetBeanGetter, String propertyName) {
        Object sourceBean = sourceBeanGetter.getBean(this);
        Object targetBean = targetBeanGetter.getBean(this);

        Class<?> sourceType = sourceBean.getClass();
        Class<?> targetType = targetBean.getClass();
        Method targetSetter = findTargetSetter(targetType, propertyName);

        if (!targetSetter.getParameterTypes()[0].isAssignableFrom(sourceType)) {
            throw new IllegalStateException(
                    "Setter for property '" + propertyName + "' on type '" + targetType.getName() + "' cannot be parameterized by an object of type '" + sourceType.getName() + "'");
        }

        try {
            targetSetter.invoke(targetBean, sourceBean);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    private Method findTargetSetter(Class<?> type, String propertyName) {
        Method targetSetter = null;
        String setterName = "set" + propertyName;

        Method[] methods = ReflectUtil.getMethods(type);
        for (Method method : methods) {
            if (method.getParameterTypes().length == 1 && method.getName().equals(setterName)) {
                targetSetter = method;
                break;
            }
        }

        if (targetSetter == null) {
            throw new IllegalStateException("Cannot find setter for property '" + propertyName + "' on type '" + type.getName() + "'");
        }

        return targetSetter;
    }
}
