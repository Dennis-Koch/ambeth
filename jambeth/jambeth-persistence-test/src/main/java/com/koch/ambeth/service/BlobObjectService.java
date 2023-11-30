package com.koch.ambeth.service;

/*-
 * #%L
 * jambeth-persistence-test
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.model.BlobObject;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;

@Service(IBlobObjectService.class)
@PersistenceContext
public class BlobObjectService implements IBlobObjectService, IInitializingBean {
    protected IDatabase database;

    protected IServiceUtil serviceUtil;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(database, "database");
        ParamChecker.assertNotNull(serviceUtil, "serviceUtil");
    }

    public void setDatabase(IDatabase database) {
        this.database = database;
    }

    public void setServiceUtil(IServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<BlobObject> getAllBlobObjects() {
        var blobObjectTable = database.getTableByType(BlobObject.class);

        var list = new ArrayList<BlobObject>();
        serviceUtil.loadObjectsIntoCollection(list, BlobObject.class, blobObjectTable.selectAll());
        return list;
    }

    @Override
    public BlobObject getBlobObject(Integer id) {
        var blobObjectTable = database.getTableByType(BlobObject.class);

        var list = new ArrayList<BlobObject>(1);
        var ids = new ArrayList<>();
        ids.add(id);
        serviceUtil.loadObjectsIntoCollection(list, BlobObject.class, blobObjectTable.selectVersion(IObjRef.PRIMARY_KEY_INDEX, ids));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<BlobObject> getBlobObjects(Integer... id) {
        var blobObjectTable = database.getTableByType(BlobObject.class);

        var list = new ArrayList<BlobObject>(id.length);
        serviceUtil.loadObjectsIntoCollection(list, BlobObject.class, blobObjectTable.selectVersion(IObjRef.PRIMARY_KEY_INDEX, new ArrayList<>(id)));
        return list;
    }

    @Override
    public void updateBlobObject(BlobObject blobObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteBlobObject(BlobObject blobObject) {
        throw new UnsupportedOperationException();
    }
}
