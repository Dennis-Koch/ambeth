package com.koch.ambeth.persistence.jdbc.lob;

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

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.model.ClobObject;
import com.koch.ambeth.persistence.jdbc.lob.ClobTest.ClobTestModule;
import com.koch.ambeth.service.ClobObjectService;
import com.koch.ambeth.service.IClobObjectService;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

@SQLStructure("clob_structure.sql")
@TestModule(ClobTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/lob/clob_orm.xml")
public class ClobTest extends AbstractInformationBusWithPersistenceTest {
    protected ClobObject createAndSaveClob(int size) {
        var clobObjectService = beanContext.getService(IClobObjectService.class);

        var clob = entityFactory.createEntity(ClobObject.class);
        var clobArray = new char[size];
        Arrays.fill(clobArray, 'a');
        clob.setContent(clobArray);

        clobObjectService.updateClobObject(clob);
        return clob;
    }

    protected ClobObject createAndSaveClob(char[] content) {
        var clobObjectService = beanContext.getService(IClobObjectService.class);

        var clob = entityFactory.createEntity(ClobObject.class);
        clob.setContent(content);

        clobObjectService.updateClobObject(clob);
        return clob;
    }

    @Test
    public void createClob() {
        var clobSize = 1234;
        var clob = createAndSaveClob(clobSize);

        Assert.assertFalse("Wrong id", clob.getId() == 0);
        Assert.assertEquals("Wrong version!", (short) 1, clob.getVersion());
        assertNotNull("Content is null", clob.getContent());
        Assert.assertEquals("Wrong size", clobSize, clob.getContent().length);
    }

    @Test
    public void updateClob() {
        var rootCache = beanContext.getService(IRootCache.class);

        var clobObjectService = beanContext.getService(IClobObjectService.class);

        var clob = createAndSaveClob(1234);

        var v1 = clob.getVersion();
        var newBlobSize = 23450;

        var clobArray = new char[newBlobSize];
        Arrays.fill(clobArray, 'b');
        clob.setContent(clobArray);
        clobObjectService.updateClobObject(clob);

        var v2 = clob.getVersion();

        Assert.assertNotSame("Version should be different", v1, v2);

        rootCache.clear(); // Clear the whole cache

        var clobObjects = clobObjectService.getClobObjects(clob.getId());

        Assert.assertNotNull("Blob collection is not valid", clobObjects);
        Assert.assertSame("Blob collection is not valid", 1, clobObjects.size());

        var reloadedBlob = clobObjects.get(0);
        Assert.assertNotNull("Reloaded clob must be valid", reloadedBlob);

        Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
        Assert.assertEquals("Blob length must have been correct", newBlobSize, reloadedBlob.getContent().length);
    }

    @Test
    public void updateClobWithContent() {
        var rootCache = beanContext.getService(IRootCache.class);

        var clobObjectService = beanContext.getService(IClobObjectService.class);

        var content = "test content".toCharArray();
        var clob = createAndSaveClob(content);

        var newContent = "new test content".toCharArray();

        clob.setContent(newContent);
        clobObjectService.updateClobObject(clob);

        rootCache.clear(); // Clear the whole cache

        var clobObjects = clobObjectService.getClobObjects(clob.getId());

        Assert.assertNotNull("Blob collection is not valid", clobObjects);
        Assert.assertSame("Blob collection is not valid", 1, clobObjects.size());

        var reloadedBlob = clobObjects.get(0);
        Assert.assertNotNull("Reloaded clob must be valid", reloadedBlob);

        Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
        Assert.assertArrayEquals("Blob content must have been correct", newContent, reloadedBlob.getContent());
    }

    @Test
    public void deleteClob() {
        var clobObjectService = beanContext.getService(IClobObjectService.class);

        var clob = createAndSaveClob(1234);

        clobObjectService.deleteClobObject(clob);

        Assert.assertEquals("Wrong id", 0, clob.getId());
        Assert.assertEquals("Wrong version", 0, clob.getVersion());

        // rootCache.clear(); // Clear the whole cache

        var clobObjects = clobObjectService.getClobObjects(clob.getId());

        Assert.assertNotNull("Blob collection is not valid", clobObjects);
        Assert.assertEquals("Blob collection is not valid", 0, clobObjects.size());
    }

    public static class ClobTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerAutowireableBean(IClobObjectService.class, ClobObjectService.class);
        }
    }
}
