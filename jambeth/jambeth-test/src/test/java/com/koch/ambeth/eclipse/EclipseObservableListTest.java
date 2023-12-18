package com.koch.ambeth.eclipse;

import com.koch.ambeth.eclipse.EclipseObservableListTest.EclipseObservableListTestPropertiesProvider;
import com.koch.ambeth.eclipse.databinding.IListChangeListenerSource;
import com.koch.ambeth.eclipse.databinding.config.EclipseDatabindingConfigurationConstants;
import com.koch.ambeth.eclipse.databinding.ioc.EclipseDatabindingModule;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.IPropertiesProvider;
import com.koch.ambeth.lazyload.EntityA;
import com.koch.ambeth.lazyload.EntityC;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.model.IDataObject;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.junit.Assert;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.List;

@TestPropertiesList({
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/lazyload/lazyloadtest_orm.xml"),
        @TestProperties(type = EclipseObservableListTestPropertiesProvider.class),
        @TestProperties(name = "ambeth.log.level.*", value = "debug")
})
@TestFrameworkModule(EclipseDatabindingModule.class)
@SQLData("com/koch/ambeth/lazyload/lazyloadtest_data.sql")
@SQLStructure("com/koch/ambeth/lazyload/lazyloadtest_structure.sql")
public class EclipseObservableListTest extends AbstractInformationBusWithPersistenceTest {

    static Realm realm = new Realm() {
        @Override
        public boolean isCurrent() {
            return true;
        }
    };
    @Autowired
    protected IRevertChangesHelper revertChangesHelper;

    @Test
    public void testGenericInterface() throws IntrospectionException {
        Class<?> enhancedType = entityMetaDataProvider.getMetaData(EntityA.class).getEnhancedType();
        BeanInfo beanInfo = Introspector.getBeanInfo(enhancedType);
        Assert.assertNotNull(beanInfo);
    }

    @Test
    public void test() {
        for (int a = 100; a-- > 0; ) {
            IQueryBuilder<EntityA> qb = queryBuilderFactory.create(EntityA.class);
            IQuery<EntityA> query = qb.build();
            final List<EntityA> entityAs = query.retrieve();

            EntityA entityA = entityAs.get(0);

            Assert.assertTrue(entityA instanceof IListChangeListenerSource);

            List<EntityC> entityCs = entityA.getEntityCs();

            WritableList list = (WritableList) entityCs;

            Assert.assertFalse(((IDataObject) entityA).isToBeUpdated());
            list.add(null); // modifying the relational collection marks the owner of the collection as
            // "dirty"
            Assert.assertTrue(((IDataObject) entityA).isToBeUpdated());
            Assert.assertEquals(3, list.size());

            revertChangesHelper.revertChanges(entityA);

            Assert.assertFalse(((IDataObject) entityA).isToBeUpdated());

            Assert.assertEquals(2, list.size());
        }
    }

    public static class EclipseObservableListTestPropertiesProvider implements IPropertiesProvider {
        @Override
        public void fillProperties(Properties props) {
            props.putIfUndefined(EclipseDatabindingConfigurationConstants.Realm, realm);
        }
    }
}
