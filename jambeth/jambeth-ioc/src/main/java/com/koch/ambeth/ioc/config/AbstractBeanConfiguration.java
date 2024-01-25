package com.koch.ambeth.ioc.config;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.link.LinkConfiguration;
import com.koch.ambeth.ioc.link.LinkController;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;

import java.util.List;

public abstract class AbstractBeanConfiguration implements IBeanConfiguration {
    protected static final LinkedHashSet<String> ignoreClassNames = new LinkedHashSet<>(0.5f);

    static {
        ignoreClassNames.add(Thread.class.getName());
        ignoreClassNames.add(AbstractBeanConfiguration.class.getName());
        ignoreClassNames.add(AbstractPropertyConfiguration.class.getName());
        ignoreClassNames.add(BeanConfiguration.class.getName());
        ignoreClassNames.add(BeanContextFactory.class.getName());
        ignoreClassNames.add(BeanRuntime.class.getName());
        ignoreClassNames.add(BeanInstanceConfiguration.class.getName());
        ignoreClassNames.add(LinkConfiguration.class.getName());
        ignoreClassNames.add(LinkController.class.getName());
        ignoreClassNames.add(ServiceContext.class.getName());
    }

    protected final IProperties props;
    protected String beanName;
    protected String parentBeanName;

    protected List<Class<?>> autowireableTypes;

    protected List<IPropertyConfiguration> propertyConfigurations;

    protected List<String> ignoredProperties;

    protected boolean overridesExistingField;

    protected PrecedenceType precedenceValue = PrecedenceType.DEFAULT;

    protected StackTraceElement[] declarationStackTrace;

    public AbstractBeanConfiguration(String beanName, IProperties props) {
        this.beanName = beanName;
        this.props = props;
        declarationStackTrace = AbstractPropertyConfiguration.getCurrentStackTraceCompact(ignoreClassNames, props);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public StackTraceElement[] getDeclarationStackTrace() {
        return declarationStackTrace;
    }

    @Override
    public PrecedenceType getPrecedence() {
        return precedenceValue;
    }

    @Override
    public IBeanConfiguration precedence(PrecedenceType precedenceType) {
        precedenceValue = precedenceType;
        return this;
    }

    @Override
    public IBeanConfiguration autowireable(Class<?> typeToPublish) {
        ParamChecker.assertParamNotNull(typeToPublish, "typeToPublish");
        if (autowireableTypes == null) {
            autowireableTypes = new ArrayList<>();
        }
        autowireableTypes.add(typeToPublish);
        return this;
    }

    @Override
    public IBeanConfiguration autowireable(Class<?>... typesToPublish) {
        ParamChecker.assertParamNotNull(typesToPublish, "typesToPublish");
        for (Class<?> typeToPublish : typesToPublish) {
            autowireable(typeToPublish);
        }
        return this;
    }

    @Override
    public IBeanConfiguration overridesExisting() {
        overridesExistingField = true;
        return this;
    }

    @Override
    public boolean isOverridesExisting() {
        return overridesExistingField;
    }

    @Override
    public IBeanConfiguration parent(String parentBeanTemplateName) {
        if (parentBeanName != null) {
            throw new UnsupportedOperationException("There is already a parent bean defined");
        }
        parentBeanName = parentBeanTemplateName;
        return this;
    }

    @Override
    public IBeanConfiguration propertyRef(String propertyName, String beanName) {
        ParamChecker.assertParamNotNull(propertyName, "propertyName");
        ParamChecker.assertParamNotNull(beanName, "beanName");
        if (propertyConfigurations == null) {
            propertyConfigurations = new ArrayList<>();
        }
        propertyConfigurations.add(new PropertyRefConfiguration(this, propertyName, null, beanName, false, props));
        return this;
    }

    @Override
    public IBeanConfiguration propertyRefFromContext(String propertyName, String fromContext, String beanName) {
        ParamChecker.assertParamNotNull(propertyName, "propertyName");
        ParamChecker.assertParamNotNull(fromContext, "fromContext");
        ParamChecker.assertParamNotNull(beanName, "beanName");
        if (propertyConfigurations == null) {
            propertyConfigurations = new ArrayList<>();
        }
        propertyConfigurations.add(new PropertyRefConfiguration(this, propertyName, fromContext, beanName, false, props));
        return this;
    }

    @Override
    public IBeanConfiguration propertyRefs(String beanName) {
        ParamChecker.assertParamNotNull(beanName, "beanName");
        if (propertyConfigurations == null) {
            propertyConfigurations = new ArrayList<>();
        }
        propertyConfigurations.add(new PropertyRefConfiguration(this, null, null, beanName, false, props));
        return this;
    }

    @Override
    public IBeanConfiguration propertyRefs(String... beanNames) {
        if (beanNames == null || beanNames.length == 0) {
            throw new IllegalArgumentException("Array of beanNames must have a length of at least 1");
        }
        for (int a = 0, size = beanNames.length; a < size; a++) {
            propertyRefs(beanNames[a]);
        }
        return this;
    }

    @Override
    public IBeanConfiguration propertyRef(String propertyName, IBeanConfiguration bean) {
        ParamChecker.assertParamNotNull(propertyName, "propertyName");
        ParamChecker.assertParamNotNull(bean, "bean");
        if (propertyConfigurations == null) {
            propertyConfigurations = new ArrayList<>();
        }
        propertyConfigurations.add(new PropertyEmbeddedRefConfiguration(this, propertyName, bean, props));
        return this;
    }

    @Override
    public IBeanConfiguration propertyRef(IBeanConfiguration bean) {
        ParamChecker.assertParamNotNull(bean, "bean");
        if (propertyConfigurations == null) {
            propertyConfigurations = new ArrayList<>();
        }
        propertyConfigurations.add(new PropertyEmbeddedRefConfiguration(this, bean, props));
        return this;
    }

    @Override
    public IBeanConfiguration propertyRefs(IBeanConfiguration... beans) {
        if (beans == null || beans.length == 0) {
            throw new IllegalArgumentException("Array of beans must have a length of at least 1");
        }
        for (int a = 0, size = beans.length; a < size; a++) {
            propertyRef(beans[a]);
        }
        return this;
    }

    @Override
    public IBeanConfiguration propertyValue(String propertyName, Object value) {
        ParamChecker.assertParamNotNull(propertyName, "propertyName");
        if (propertyConfigurations == null) {
            propertyConfigurations = new ArrayList<>();
        }
        propertyConfigurations.add(new PropertyValueConfiguration(this, propertyName, value, props));
        return this;
    }

    @Override
    public IBeanConfiguration ignoreProperties(String propertyName) {
        ParamChecker.assertParamNotNull(propertyName, "propertyName");
        if (ignoredProperties == null) {
            ignoredProperties = new ArrayList<>();
        }
        ignoredProperties.add(propertyName);
        return this;
    }

    @Override
    public IBeanConfiguration ignoreProperties(String... propertyNames) {
        if (propertyNames == null || propertyNames.length == 0) {
            throw new IllegalArgumentException("Array of propertyNames must have a length of at least 1");
        }
        for (int a = 0, size = propertyNames.length; a < size; a++) {
            ignoreProperties(propertyNames[a]);
        }
        return this;
    }

    @Override
    public String getName() {
        return beanName;
    }

    @Override
    public String getParentName() {
        return parentBeanName;
    }

    @Override
    public boolean isWithLifecycle() {
        return true;
    }

    @Override
    public List<Class<?>> getAutowireableTypes() {
        return autowireableTypes;
    }

    @Override
    public List<IPropertyConfiguration> getPropertyConfigurations() {
        return propertyConfigurations;
    }

    @Override
    public List<String> getIgnoredPropertyNames() {
        return ignoredProperties;
    }

    @Override
    public Object getInstance() {
        return getInstance(getBeanType());
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public IBeanConfiguration template() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract Class<?> getBeanType();

    @Override
    public String toString() {
        String name = getName();
        return name != null ? name : super.toString();
    }
}
