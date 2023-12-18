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

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.xml.model.Address;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SQLData("/com/koch/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/xml/Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/orm.xml")
public class ORIHelperValueHolderTest extends AbstractInformationBusWithPersistenceTest {
    protected ICache cache;

    protected IObjRefHelper oriHelper;

    protected IProxyHelper proxyHelper;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        ParamChecker.assertNotNull(cache, "cache");
        ParamChecker.assertNotNull(oriHelper, "oriHelper");
        ParamChecker.assertNotNull(proxyHelper, "proxyHelper");
    }

    public void setCache(ICache cache) {
        this.cache = cache;
    }

    public void setOriHelper(IObjRefHelper oriHelper) {
        this.oriHelper = oriHelper;
    }

    public void setProxyHelper(IProxyHelper proxyHelper) {
        this.proxyHelper = proxyHelper;
    }

    @Test
    public void testExtractOrisFromListOfValueHolders() throws Throwable {
        Employee employee1 = cache.getObject(Employee.class, 1);
        Employee employee2 = cache.getObject(Employee.class, 2);

        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Employee.class);
        int relationIndex = metaData.getIndexByRelationName("PrimaryAddress");

        assertTrue(employee1 instanceof IObjRefContainer);
        assertTrue(employee2 instanceof IObjRefContainer);
        assertTrue(!((IObjRefContainer) employee1).is__Initialized(relationIndex));
        assertTrue(!((IObjRefContainer) employee2).is__Initialized(relationIndex));

        var extractedORIList = new ArrayList<IObjRef>();
        extractedORIList.addAll(((IObjRefContainer) employee1).get__ObjRefs(relationIndex));
        extractedORIList.addAll(((IObjRefContainer) employee2).get__ObjRefs(relationIndex));

        assertEquals(2, extractedORIList.size());

        Address address1 = employee1.getPrimaryAddress().get();
        Address address2 = employee2.getPrimaryAddress().get();

        var ori1 = extractedORIList.get(0);
        assertEquals(Address.class, ori1.getRealType());
        assertEquals(address1.getId(), ori1.getId());
        assertEquals(ObjRef.PRIMARY_KEY_INDEX, ori1.getIdNameIndex());
        Assert.assertNull(ori1.getVersion());

        IObjRef ori2 = extractedORIList.get(1);
        assertEquals(Address.class, ori2.getRealType());
        assertEquals(address2.getId(), ori2.getId());
        assertEquals(ObjRef.PRIMARY_KEY_INDEX, ori2.getIdNameIndex());
        Assert.assertNull(ori2.getVersion());
    }
}
