package com.koch.ambeth.persistence.blueprint;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.audit.server.IAuditConfigurationProvider;
import com.koch.ambeth.audit.server.ioc.AuditModule;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.mapping.ioc.MappingModule;
import com.koch.ambeth.merge.ITechnicalEntityTypeExtendable;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintOrmProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintVomProvider;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationPropertyBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityPropertyBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.persistence.blueprint.OrmBlueprintTest.OrmBlueprintTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.xml.ioc.XmlBlueprintModule;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.orm.blueprint.JavassistOrmEntityTypeProvider;
import org.junit.Assert;
import org.junit.Test;

@SQLStructure("OrmBlueprint_structure.sql")
@SQLData("OrmBlueprint_data.sql")
@TestPropertiesList({
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/blueprint/orm.xml"),
        @TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true")
})
@TestFrameworkModule({ XmlModule.class, MappingModule.class, AuditModule.class })
@TestModule({ XmlBlueprintModule.class, OrmBlueprintTestModule.class })
public class OrmBlueprintTest extends AbstractInformationBusWithPersistenceTest {
    public static final String AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS = "com.koch.ambeth.persistence.blueprint.TestClass";
    public static final String AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP = "Something";

    @Autowired
    protected IAuditConfigurationProvider auditConfigurationProvider;

    @Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
    protected JavassistOrmEntityTypeProvider entityTypeProvider;

    @Autowired
    protected IMapperServiceFactory mapperServiceFactory;

    @Autowired
    protected IPropertyInfoProvider propertyInfoProvider;

    @LogInstance
    private ILogger log;

    @Test
    public void testInitiatiateBlueprintedEntity() throws Throwable {
        var resolveEntityType = entityTypeProvider.resolveEntityType(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
        Assert.assertNotNull(resolveEntityType);

        var entity = entityFactory.createEntity(resolveEntityType);
        var properties = propertyInfoProvider.getProperties(entity);

        IPropertyInfo prop = null;
        for (var propertyInfo : properties) {
            if (propertyInfo.getName().equals(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP)) {
                prop = propertyInfo;
                break;
            }
        }
        Assert.assertNotNull(prop);
        prop.setValue(entity, "TestValue");

        Assert.assertEquals("TestValue", prop.getValue(entity));

        var auditConfiguration = auditConfigurationProvider.getAuditConfiguration(resolveEntityType);
        Assert.assertTrue(auditConfiguration.isAuditActive());
    }

    @Test
    public void testVomMappingBlueprintedEntity() throws Throwable {
        var resolveEntityType = entityTypeProvider.resolveEntityType(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
        Assert.assertNotNull(resolveEntityType);

        var entity = entityFactory.createEntity(resolveEntityType);

        var prop = propertyInfoProvider.getProperty(entity, AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
        Assert.assertNotNull(prop);
        var testValue = "TestValue";
        prop.setValue(entity, testValue);

        var valueObjectType = entityTypeProvider.resolveEntityType(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS + "V");
        Assert.assertFalse(valueObjectType.isInterface());
        var mapper = mapperServiceFactory.create();
        try {
            var valueObject = mapper.mapToValueObject(entity, valueObjectType);
            var vomProperty = propertyInfoProvider.getProperty(valueObject, AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
            Assert.assertEquals(testValue, vomProperty.getValue(valueObject));
        } finally {
            mapper.dispose();
        }
    }

    public static class OrmBlueprintTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.link(IEntityTypeBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityTypeBlueprint.class);
            beanContextFactory.link(IEntityPropertyBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityPropertyBlueprint.class);
            beanContextFactory.link(IEntityAnnotationBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityAnnotationBlueprint.class);
            beanContextFactory.link(IEntityAnnotationPropertyBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityAnnotationPropertyBlueprint.class);
            beanContextFactory.registerBean(SQLOrmBlueprintProvider.class).autowireable(IBlueprintProvider.class, IBlueprintOrmProvider.class, IBlueprintVomProvider.class);
            beanContextFactory.registerBean(OrmVomDocumentCreator.class).autowireable(IVomDocumentCreator.class, IOrmDocumentCreator.class);
            beanContextFactory.registerBean(EntityTypeBluePrintService.class).autowireable(EntityTypeBluePrintService.class);
        }
    }
}
