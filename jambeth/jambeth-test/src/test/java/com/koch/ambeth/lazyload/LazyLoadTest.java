package com.koch.ambeth.lazyload;

import com.koch.ambeth.cache.ValueHolderIEC;
import com.koch.ambeth.cache.mixin.IAsyncLazyLoadController;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.lazyload.LazyLoadTest.LazyLoadTestModule;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.model.IEmbeddedType;
import com.koch.ambeth.util.model.INotifyPropertyChanged;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@TestPropertiesList({
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "lazyloadtest_orm.xml"), @TestProperties(name = "ambeth.log.level.*", value = "debug")
})
@SQLData("lazyloadtest_data.sql")
@SQLStructure("lazyloadtest_structure.sql")
@TestModule(LazyLoadTestModule.class)
public class LazyLoadTest extends AbstractInformationBusWithPersistenceTest {
    @Autowired
    protected IAsyncLazyLoadController asyncLazyLoadController;
    @Autowired
    protected IGuiThreadHelper guiThreadHelper;
    @Autowired
    protected IQueryBuilderFactory queryBuilderFactory;

    @Test
    public void testLazyLoadFromUiThread() throws Throwable {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(guiThreadHelper.isInGuiThread());
            }
        });
        Assert.assertFalse(guiThreadHelper.isInGuiThread());

        IQueryBuilder<EntityA> qb = queryBuilderFactory.create(EntityA.class);
        IQuery<EntityA> query = qb.build();
        final List<EntityA> entityAs = query.retrieve();

        Assert.assertEquals(2, entityAs.size());

        final CountDownLatch latch = new CountDownLatch(entityAs.size() * 2);
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(EntityA.class);
        PropertyChangeListener entityBPcl = WaitingForInitPCL.create(metaData, "EmbeddedA.EntityB", pce -> {
            latch.countDown();
        });
        PropertyChangeListener entityCPcl = WaitingForInitPCL.create(metaData, "EntityCs", pce -> {
            latch.countDown();
        });
        for (EntityA entityA : entityAs) {
            ((INotifyPropertyChanged) entityA.getEmbeddedA()).addPropertyChangeListener(entityBPcl);
            ((INotifyPropertyChanged) entityA).addPropertyChangeListener(entityCPcl);
        }
        EventQueue.invokeAndWait(() -> {
            var rollback = asyncLazyLoadController.pushAsynchronousResultAllowed();
            try {
                // entityA.getEntityB();
                for (var entityA : entityAs) {
                    entityA.getEmbeddedA().getEntityB();
                    entityA.getEntityCs();
                }
            } finally {
                rollback.rollback();
            }
        });
        latch.await();
    }

    public static class WaitingForInitPCL implements PropertyChangeListener {
        public static PropertyChangeListener create(IEntityMetaData metaData, String propertyPath, PropertyChangeListener target) {
            int relationIndex = metaData.getIndexByRelationName(propertyPath);
            int lastDot = propertyPath.lastIndexOf('.');
            if (lastDot != -1) {
                propertyPath = propertyPath.substring(lastDot + 1);
            }
            return new WaitingForInitPCL(Introspector.decapitalize(ValueHolderIEC.getInitializedFieldName(propertyPath)), relationIndex, target);
        }

        private final int relationIndex;
        private final String relationStateName;
        private PropertyChangeListener target;

        public WaitingForInitPCL(String relationStateName, int relationIndex, PropertyChangeListener target) {
            this.relationStateName = relationStateName;
            this.relationIndex = relationIndex;
            this.target = target;
        }

        public PropertyChangeListener getTarget() {
            return target;
        }

        public void setTarget(PropertyChangeListener target) {
            this.target = target;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!relationStateName.equals(evt.getPropertyName())) {
                return;
            }
            Object newValue = evt.getNewValue();
            if (newValue == null) {
                Object source = evt.getSource();
                if (source instanceof IEmbeddedType) {
                    source = ((IEmbeddedType) source).getRoot();
                }
                newValue = ((IObjRefContainer) source).get__State(relationIndex);
            }
            if (newValue == ValueHolderState.INIT) {
                target.propertyChange(evt);
            }
        }
    }

    public static class LazyLoadTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        }
    }
}
