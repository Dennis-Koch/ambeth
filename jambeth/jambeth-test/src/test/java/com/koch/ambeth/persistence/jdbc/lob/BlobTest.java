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

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.model.BlobObject;
import com.koch.ambeth.persistence.jdbc.lob.BlobTest.BlobTestModule;
import com.koch.ambeth.service.BlobObjectService;
import com.koch.ambeth.service.IBlobObjectService;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@SQLStructure("blob_structure.sql")
@TestModule(BlobTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/lob/blob_orm.xml")
public class BlobTest extends AbstractInformationBusWithPersistenceTest {
    @Autowired
    protected IEventDispatcher eventDispatcher;

    protected BlobObject createAndSaveBlob(int size) {
        var blobObjectService = beanContext.getService(IBlobObjectService.class);

        var blob = entityFactory.createEntity(BlobObject.class);
        if (size < 0) {
            blob.setContent(null);
        } else {
            blob.setContent(new byte[size]);
        }

        blobObjectService.updateBlobObject(blob);
        return blob;
    }

    protected BlobObject createAndSaveBlob(byte[] content) {
        var blobObjectService = beanContext.getService(IBlobObjectService.class);

        var blob = entityFactory.createEntity(BlobObject.class);
        blob.setContent(content);

        blobObjectService.updateBlobObject(blob);
        return blob;
    }

    @Test
    public void createNullBlob() {
        var blob = createAndSaveBlob(-1);

        Assert.assertFalse("Wrong id", blob.getId() == 0);
        Assert.assertEquals("Wrong version!", (short) 1, blob.getVersion());
        Assert.assertNull("Content must be null", blob.getContent());
    }

    @Test
    public void readNullBlob() {
        var blob = createAndSaveBlob(-1);

        eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

        var blobObjectService = beanContext.getService(IBlobObjectService.class);

        var blobObject = blobObjectService.getBlobObject(blob.getId());
        Assert.assertNotNull(blobObject);

    }

    @Test
    public void createBlob() {
        var blobSize = 1234;
        var blob = createAndSaveBlob(blobSize);

        Assert.assertFalse("Wrong id", blob.getId() == 0);
        Assert.assertEquals("Wrong version!", (short) 1, blob.getVersion());
        assertNotNull("Content is null", blob.getContent());
        Assert.assertEquals("Wrong size", blobSize, blob.getContent().length);
    }

    @Test
    public void updateBlob() {
        var blobObjectService = beanContext.getService(IBlobObjectService.class);

        var blob = createAndSaveBlob(1234);

        var v1 = blob.getVersion();
        var content = new byte[23450];
        for (int a = content.length; a-- > 0; ) {
            content[a] = (byte) (Math.random() * Short.MAX_VALUE);
        }
        blob.setContent(content);

        blobObjectService.updateBlobObject(blob);

        var v2 = blob.getVersion();

        Assert.assertNotSame("Version should be different", v1, v2);

        eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

        var blobObjects = blobObjectService.getBlobObjects(blob.getId());

        Assert.assertNotNull("Blob collection is not valid", blobObjects);
        Assert.assertSame("Blob collection is not valid", 1, blobObjects.size());

        var reloadedBlob = blobObjects.get(0);
        Assert.assertNotNull("Reloaded blob must be valid", reloadedBlob);

        Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
        Assert.assertEquals("Blob length must have been correct", content.length, reloadedBlob.getContent().length);
        Assert.assertArrayEquals("Blob content must be equal", content, reloadedBlob.getContent());
    }

    @Test
    public void updateBlobWithContent() {
        var blobObjectService = beanContext.getService(IBlobObjectService.class);

        byte[] content = { 1, 2, 3, 4, 5 };
        var blob = createAndSaveBlob(content);

        byte[] newContent = { 1, 1, 2, 3, 5, 8 };

        blob.setContent(newContent);
        blobObjectService.updateBlobObject(blob);

        eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

        var blobObjects = blobObjectService.getBlobObjects(blob.getId());

        Assert.assertNotNull("Blob collection is not valid", blobObjects);
        Assert.assertSame("Blob collection is not valid", 1, blobObjects.size());

        BlobObject reloadedBlob = blobObjects.get(0);
        Assert.assertNotNull("Reloaded clob must be valid", reloadedBlob);

        Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
        Assert.assertArrayEquals("Blob content must have been correct", newContent, reloadedBlob.getContent());
    }

    @Test
    public void deleteBlob() {
        var blobObjectService = beanContext.getService(IBlobObjectService.class);

        var blob = createAndSaveBlob(1234);

        blobObjectService.deleteBlobObject(blob);

        Assert.assertEquals("Wrong id", 0, blob.getId());
        Assert.assertEquals("Wrong version", 0, blob.getVersion());

        var blobObjects = blobObjectService.getAllBlobObjects();

        Assert.assertNotNull("Blob collection is not valid", blobObjects);
        Assert.assertEquals("Blob collection is not valid", 0, blobObjects.size());
    }

    public static class BlobTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerAutowireableBean(IBlobObjectService.class, BlobObjectService.class);
        }
    }
}
