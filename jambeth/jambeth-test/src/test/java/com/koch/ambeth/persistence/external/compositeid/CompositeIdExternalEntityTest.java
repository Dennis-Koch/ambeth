package com.koch.ambeth.persistence.external.compositeid;

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

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.cache.stream.CacheRetrieverFake;
import com.koch.ambeth.event.server.ioc.EventServerModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.external.compositeid.CompositeIdExternalEntityTest.CompositeIdExternalEntityTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.ParamChecker;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/external/compositeid/external_orm.xml")
@TestFrameworkModule({ CompositeIdExternalEntityTestModule.class, EventServerModule.class })
public class CompositeIdExternalEntityTest extends AbstractInformationBusTest {
    protected ICache cache;
    protected ICompositeIdFactory compositeIdFactory;
    protected IEntityFactory entityFactory;
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        ParamChecker.assertNotNull(cache, "cache");
        ParamChecker.assertNotNull(compositeIdFactory, "compositeIdFactory");
        ParamChecker.assertNotNull(entityFactory, "entityFactory");
        ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
    }

    public void setCache(ICache cache) {
        this.cache = cache;
    }

    public void setCompositeIdFactory(ICompositeIdFactory compositeIdFactory) {
        this.compositeIdFactory = compositeIdFactory;
    }

    public void setEntityFactory(IEntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider) {
        this.entityMetaDataProvider = entityMetaDataProvider;
    }

    @Test
    public void testCompositeIdBehaviorEquals() throws Exception {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);
        Object left = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0], CompositeIdEntityCacheRetriever.id1_2_data[1]);
        Object right = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0], CompositeIdEntityCacheRetriever.id1_2_data[1]);
        Assert.assertNotNull(left);
        Assert.assertNotNull(right);
        Assert.assertNotSame(left, right);
        Assert.assertEquals(left, right);
        Assert.assertTrue(left instanceof IPrintable);
        StringBuilder sb = new StringBuilder();
        ((IPrintable) left).toString(sb);
        Assert.assertEquals(left.toString(), sb.toString());
    }

    @Test
    public void testCompositeIdBehaviorNotEqual() throws Exception {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);
        Object left = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0], CompositeIdEntityCacheRetriever.id1_2_data[1]);
        Object right = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), ((Number) CompositeIdEntityCacheRetriever.id1_2_data[0]).intValue() + 2,
                CompositeIdEntityCacheRetriever.id1_2_data[1]);
        Assert.assertNotNull(left);
        Assert.assertNotNull(right);
        Assert.assertNotSame(left, right);
        Assert.assertFalse(left.equals(right));
        int idIndex = 0;
        Object right2 = compositeIdFactory.createCompositeId(metaData, metaData.getAlternateIdMembers()[idIndex], CompositeIdEntityCacheRetriever.id1_2_data[4],
                ((Number) CompositeIdEntityCacheRetriever.id1_2_data[3]).shortValue() + 2);
        Assert.assertNotNull(right2);
        Assert.assertNotSame(left, right2);
        Assert.assertFalse(left.equals(right2));
    }

    @Test
    public void testPrimaryId() throws Exception {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);
        Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0], CompositeIdEntityCacheRetriever.id1_2_data[1]);
        CompositeIdEntity entity = cache.getObject(CompositeIdEntity.class, compositeId);
        Assert.assertNotNull(entity);
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[0], entity.getId1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[1], entity.getId2());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[2], entity.getName());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[3], entity.getAid1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[4], entity.getAid2());
    }

    @Test
    public void testAlternateId() throws Exception {
        int idIndex = 0;
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);
        Object compositeId =
                compositeIdFactory.createCompositeId(metaData, metaData.getAlternateIdMembers()[idIndex], CompositeIdEntityCacheRetriever.id1_2_data[4], CompositeIdEntityCacheRetriever.id1_2_data[3]);
        CompositeIdEntity entity = (CompositeIdEntity) cache.getObject(new ObjRef(CompositeIdEntity.class, (byte) idIndex, compositeId, null), CacheDirective.none());
        Assert.assertNotNull(entity);
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[0], entity.getId1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[1], entity.getId2());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[2], entity.getName());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[3], entity.getAid1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[4], entity.getAid2());
    }

    @Test
    public void testPrimaryIdEmbedded() throws Exception {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity2.class);
        Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0], CompositeIdEntityCacheRetriever.id1_2_data[1]);
        CompositeIdEntity2 entity = cache.getObject(CompositeIdEntity2.class, compositeId);
        Assert.assertNotNull(entity);
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[0], entity.getId1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[1], entity.getId2().getSid());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[2], entity.getName());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[3], entity.getAid1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[4], entity.getAid2().getSid());
    }

    @Test
    public void testAlternateIdEmbedded() throws Exception {
        int idIndex = 0;
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity2.class);
        Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getAlternateIdMembers()[idIndex], CompositeIdEntityCacheRetriever.entity2_id1_2_data[4],
                CompositeIdEntityCacheRetriever.entity2_id1_2_data[3]);
        CompositeIdEntity2 entity = (CompositeIdEntity2) cache.getObject(new ObjRef(CompositeIdEntity2.class, (byte) idIndex, compositeId, null), CacheDirective.none());
        Assert.assertNotNull(entity);
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[0], entity.getId1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[1], entity.getId2().getSid());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[2], entity.getName());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[3], entity.getAid1());
        Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[4], entity.getAid2().getSid());
    }

    @FrameworkModule
    public static class CompositeIdExternalEntityTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(CacheModule.DEFAULT_CACHE_RETRIEVER, CacheRetrieverFake.class);

            IBeanConfiguration bc = beanContextFactory.registerBean(CompositeIdEntityCacheRetriever.class);
            beanContextFactory.link(bc).to(ICacheRetrieverExtendable.class).with(CompositeIdEntity.class);
            beanContextFactory.link(bc).to(ICacheRetrieverExtendable.class).with(CompositeIdEntity2.class);
        }
    }
}
