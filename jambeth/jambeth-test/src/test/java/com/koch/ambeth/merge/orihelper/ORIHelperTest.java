package com.koch.ambeth.merge.orihelper;

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

import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.independent.EntityB;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@TestPropertiesList({
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/merge/orihelper/ORIHelperTest-orm.xml")
})
@SQLStructure("ORIHelperTest_structure.sql")
@TestModule({ ORIHelperTestModule.class })
public class ORIHelperTest extends AbstractInformationBusWithPersistenceTest {
    private ORIHelperTestService oriHelperTestService;

    private ICache cache;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        ParamChecker.assertNotNull(cache, "cache");
    }

    public void setOriHelperTestService(ORIHelperTestService oriHelperTestService) {
        this.oriHelperTestService = oriHelperTestService;
    }

    public void setCache(ICache cache) {
        this.cache = cache;
    }

    @Test
    public void testGetCreateORI() {
        List<IObjRef> oris = new ArrayList<>();

        oris.add(new ObjRef(EntityB.class, 1, 1));

        oriHelperTestService.getAllEntityBs();
        List<Object> objects = cache.getObjects(oris, CacheDirective.returnMisses());

        List<Object> objects2 = cache.getObjects(oris, CacheDirective.none());

        Assert.assertEquals(0, objects2.size());
        Assert.assertEquals(oris.size(), objects.size());
    }

}
