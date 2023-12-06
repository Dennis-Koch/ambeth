package com.koch.ambeth.cache.stream;

/*-
 * #%L
 * jambeth-cache-stream
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

import com.koch.ambeth.cache.IParentEntityAware;
import com.koch.ambeth.cache.chunk.ChunkProviderStubInputStream;
import com.koch.ambeth.cache.chunk.IChunkProvider;
import com.koch.ambeth.cache.transfer.ObjRelation;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.stream.IInputSource;
import com.koch.ambeth.util.ParamChecker;

import java.io.BufferedInputStream;
import java.io.InputStream;

public abstract class AbstractInputSourceValueHolder implements IInputSource, IParentEntityAware, IInitializingBean {
    protected int chunkSize = 65536;

    protected IServiceContext beanContext;

    protected String chunkProviderName;

    protected Member member;

    protected Object parentEntity;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(beanContext, "beanContext");
        ParamChecker.assertNotNull(chunkProviderName, "chunkProviderName");
    }

    public void setBeanContext(IServiceContext beanContext) {
        this.beanContext = beanContext;
    }

    public void setChunkProviderName(String chunkProviderName) {
        this.chunkProviderName = chunkProviderName;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public void setParentEntity(Object parentEntity, Member member) {
        this.parentEntity = parentEntity;
        this.member = member;
    }

    public IObjRelation getSelf() {
        var oriHelper = beanContext.getService(IObjRefHelper.class);
        var allObjRefs = oriHelper.entityToAllObjRefs(parentEntity);
        return new ObjRelation(allObjRefs.toArray(IObjRef[]::new), member.getName());
    }

    protected IChunkProvider getChunkProvider() {
        return beanContext.getService(chunkProviderName, IChunkProvider.class);
    }

    protected InputStream createBinaryInputStream() {
        IChunkProvider chunkProvider = getChunkProvider();
        return new BufferedInputStream(new ChunkProviderStubInputStream(getSelf(), chunkProvider), chunkSize);
    }
}
