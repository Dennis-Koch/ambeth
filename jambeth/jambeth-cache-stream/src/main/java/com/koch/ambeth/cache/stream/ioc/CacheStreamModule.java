package com.koch.ambeth.cache.stream.ioc;

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

import io.toolisticon.spiap.api.SpiService;
import com.koch.ambeth.cache.stream.AbstractInputSourceConverter;
import com.koch.ambeth.cache.stream.bool.BooleanInputSourceConverter;
import com.koch.ambeth.cache.stream.bytebuffer.FileContentCache;
import com.koch.ambeth.cache.stream.bytebuffer.FileHandleCache;
import com.koch.ambeth.cache.stream.bytebuffer.IFileContentCache;
import com.koch.ambeth.cache.stream.bytebuffer.IFileHandleCache;
import com.koch.ambeth.cache.stream.float32.FloatInputSourceConverter;
import com.koch.ambeth.cache.stream.float64.DoubleInputSourceConverter;
import com.koch.ambeth.cache.stream.int32.IntInputSourceConverter;
import com.koch.ambeth.cache.stream.int64.LongInputSourceConverter;
import com.koch.ambeth.cache.stream.strings.StringInputSourceConverter;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.stream.IInputSource;
import com.koch.ambeth.stream.IInputSourceTemplate;
import com.koch.ambeth.stream.bool.IBooleanInputSource;
import com.koch.ambeth.stream.float32.IFloatInputSource;
import com.koch.ambeth.stream.float64.IDoubleInputSource;
import com.koch.ambeth.stream.int32.IIntInputSource;
import com.koch.ambeth.stream.int64.ILongInputSource;
import com.koch.ambeth.stream.strings.IStringInputSource;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class CacheStreamModule implements IFrameworkModule {
    public static final String CHUNK_PROVIDER_NAME = "chunkProvider";
    protected String chunkProviderName = CHUNK_PROVIDER_NAME;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        if (chunkProviderName == null) {
            if (log.isWarnEnabled()) {
                log.warn("Chunked streaming feature disabled: No property 'ChunkProviderName' defined");
            }
            return;
        }
        registerInputSourceConverter(beanContextFactory, BooleanInputSourceConverter.class, IBooleanInputSource.class);
        registerInputSourceConverter(beanContextFactory, DoubleInputSourceConverter.class, IDoubleInputSource.class);
        registerInputSourceConverter(beanContextFactory, FloatInputSourceConverter.class, IFloatInputSource.class);
        registerInputSourceConverter(beanContextFactory, IntInputSourceConverter.class, IIntInputSource.class);
        registerInputSourceConverter(beanContextFactory, LongInputSourceConverter.class, ILongInputSource.class);
        registerInputSourceConverter(beanContextFactory, StringInputSourceConverter.class, IStringInputSource.class);

        IBeanConfiguration fileContentCache = beanContextFactory.registerBean(FileContentCache.class).autowireable(IFileContentCache.class);
        beanContextFactory.link(fileContentCache, FileContentCache.HANDLE_CLEAR_ALL_CACHES).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

        IBeanConfiguration fileHandleCache = beanContextFactory.registerBean(FileHandleCache.class).autowireable(IFileHandleCache.class);
        beanContextFactory.link(fileHandleCache, FileHandleCache.HANDLE_CLEAR_ALL_CACHES).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);
    }

    public void setChunkProviderName(String chunkProviderName) {
        this.chunkProviderName = chunkProviderName;
    }

    public IBeanConfiguration registerInputSourceConverter(IBeanContextFactory beanContextFactory, Class<? extends AbstractInputSourceConverter> converterType,
            Class<? extends IInputSource> inputSourceType) {
        IBeanConfiguration converterBC = beanContextFactory.registerBean(converterType).propertyValue("ChunkProviderName", chunkProviderName);
        beanContextFactory.link(converterBC).to(IDedicatedConverterExtendable.class).with(IInputSourceTemplate.class, inputSourceType);
        return converterBC;
    }
}
