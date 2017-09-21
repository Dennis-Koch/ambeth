package com.koch.ambeth.persistence.streaming;

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
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.stream.CacheRetrieverFake;
import com.koch.ambeth.cache.stream.InputSourceTemplateFake;
import com.koch.ambeth.cache.stream.InputSourceTemplateFakeConverter;
import com.koch.ambeth.cache.stream.ioc.CacheStreamModule;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.event.server.ioc.EventServerModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.streaming.StreamingEntityTest.StreamingEntityTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.bool.IBooleanInputStream;
import com.koch.ambeth.stream.float32.IFloatInputStream;
import com.koch.ambeth.stream.float64.IDoubleInputStream;
import com.koch.ambeth.stream.int32.IIntInputStream;
import com.koch.ambeth.stream.int64.ILongInputStream;
import com.koch.ambeth.stream.strings.IStringInputStream;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/streaming/streaming_orm.xml")
@TestFrameworkModule({StreamingEntityTestModule.class, EventServerModule.class})
@TestRebuildContext
public class StreamingEntityTest extends AbstractInformationBusTest {
	@FrameworkModule
	public static class StreamingEntityTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(CacheModule.DEFAULT_CACHE_RETRIEVER, CacheRetrieverFake.class)
					.autowireable(ICacheRetriever.class);

			beanContextFactory.registerAlias(CacheStreamModule.CHUNK_PROVIDER_NAME,
					CacheModule.DEFAULT_CACHE_RETRIEVER);
			IBeanConfiguration istfConverter =
					beanContextFactory.registerBean(InputSourceTemplateFakeConverter.class);
			beanContextFactory.link(istfConverter).to(IDedicatedConverterExtendable.class)
					.with(InputSourceTemplateFake.class, IBinaryInputStream.class);
		}
	}

	@Autowired(CacheModule.DEFAULT_CACHE_RETRIEVER)
	protected CacheRetrieverFake cacheRetrieverFake;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Test
	public void streamedBoolean() throws Exception {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(StreamingEntity.class);

		LoadContainer lc = new LoadContainer();
		ObjRef objRef = new ObjRef(StreamingEntity.class, new Integer(1), null);
		lc.setReference(objRef);

		boolean[] expected = {true, false, true, true, false};

		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);
		lc.getPrimitives()[metaData.getIndexByPrimitiveName("BooleanStreamed")] =
				new InputSourceTemplateFake(expected);

		cacheRetrieverFake.entities.put(lc.getReference(), lc);

		StreamingEntity streamingEntity = cache.getObject(StreamingEntity.class, 1);
		IBooleanInputStream is = streamingEntity.getBooleanStreamed().deriveBooleanInputStream();
		try {
			int index = 0;
			while (is.hasBoolean()) {
				boolean value = is.readBoolean();
				Assert.assertEquals(expected[index++], value);
			}
		}
		finally {
			is.close();
		}
		Assert.assertNotNull(streamingEntity);
	}

	@Test
	public void streamedDouble() throws Exception {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(StreamingEntity.class);

		LoadContainer lc = new LoadContainer();
		ObjRef objRef = new ObjRef(StreamingEntity.class, new Integer(1), null);
		lc.setReference(objRef);

		double[] expected = {5, 4, Double.MAX_VALUE, Double.MIN_VALUE, 0, -1, 5.4321};

		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);
		lc.getPrimitives()[metaData.getIndexByPrimitiveName("DoubleStreamed")] =
				new InputSourceTemplateFake(expected);

		cacheRetrieverFake.entities.put(lc.getReference(), lc);

		StreamingEntity streamingEntity = cache.getObject(StreamingEntity.class, 1);
		IDoubleInputStream is = streamingEntity.getDoubleStreamed().deriveDoubleInputStream();
		try {
			int index = 0;
			while (is.hasDouble()) {
				double value = is.readDouble();
				Assert.assertEquals(expected[index++], value, Double.MIN_VALUE);
			}
		}
		finally {
			is.close();
		}
		Assert.assertNotNull(streamingEntity);
	}

	@Test
	public void streamedFloat() throws Exception {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(StreamingEntity.class);

		LoadContainer lc = new LoadContainer();
		ObjRef objRef = new ObjRef(StreamingEntity.class, new Integer(1), null);
		lc.setReference(objRef);

		float[] expected = {5, 4, Float.MAX_VALUE, Float.MIN_VALUE, 0, -1, 5.4321f};

		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);
		lc.getPrimitives()[metaData.getIndexByPrimitiveName("FloatStreamed")] =
				new InputSourceTemplateFake(expected);

		cacheRetrieverFake.entities.put(lc.getReference(), lc);

		StreamingEntity streamingEntity = cache.getObject(StreamingEntity.class, 1);
		IFloatInputStream is = streamingEntity.getFloatStreamed().deriveFloatInputStream();
		try {
			int index = 0;
			while (is.hasFloat()) {
				float value = is.readFloat();
				Assert.assertEquals(expected[index++], value, Float.MIN_VALUE);
			}
		}
		finally {
			is.close();
		}
		Assert.assertNotNull(streamingEntity);
	}

	@Test
	public void streamedInt() throws Exception {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(StreamingEntity.class);

		LoadContainer lc = new LoadContainer();
		ObjRef objRef = new ObjRef(StreamingEntity.class, new Integer(1), null);
		lc.setReference(objRef);

		int[] expected = {5, 4, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, -1};

		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);
		lc.getPrimitives()[metaData.getIndexByPrimitiveName("IntStreamed")] =
				new InputSourceTemplateFake(expected);

		cacheRetrieverFake.entities.put(lc.getReference(), lc);

		StreamingEntity streamingEntity = cache.getObject(StreamingEntity.class, 1);
		IIntInputStream is = streamingEntity.getIntStreamed().deriveIntInputStream();
		try {
			int index = 0;
			while (is.hasInt()) {
				int value = is.readInt();
				Assert.assertEquals(expected[index++], value);
			}
		}
		finally {
			is.close();
		}
		Assert.assertNotNull(streamingEntity);
	}

	@Test
	public void streamedLong() throws Exception {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(StreamingEntity.class);

		LoadContainer lc = new LoadContainer();
		ObjRef objRef = new ObjRef(StreamingEntity.class, new Integer(1), null);
		lc.setReference(objRef);

		long[] expected = {5, 4, Long.MAX_VALUE, Long.MIN_VALUE, 0, -1};

		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);
		lc.getPrimitives()[metaData.getIndexByPrimitiveName("LongStreamed")] =
				new InputSourceTemplateFake(expected);

		cacheRetrieverFake.entities.put(lc.getReference(), lc);

		StreamingEntity streamingEntity = cache.getObject(StreamingEntity.class, 1);
		ILongInputStream is = streamingEntity.getLongStreamed().deriveLongInputStream();
		try {
			int index = 0;
			while (is.hasLong()) {
				long value = is.readLong();
				Assert.assertEquals(expected[index++], value);
			}
		}
		finally {
			is.close();
		}
		Assert.assertNotNull(streamingEntity);
	}

	@Test
	public void streamedString() throws Exception {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(StreamingEntity.class);

		LoadContainer lc = new LoadContainer();
		ObjRef objRef = new ObjRef(StreamingEntity.class, new Integer(1), null);
		lc.setReference(objRef);

		String[] expected =
				{Integer.toString(5), Integer.toString(4), Integer.toString(Integer.MAX_VALUE), null,
						Integer.toString(Integer.MAX_VALUE), Integer.toString(0), Integer.toString(-1)};

		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);
		lc.getPrimitives()[metaData.getIndexByPrimitiveName("StringStreamed")] =
				new InputSourceTemplateFake(expected);

		cacheRetrieverFake.entities.put(lc.getReference(), lc);

		StreamingEntity streamingEntity = cache.getObject(StreamingEntity.class, 1);
		IStringInputStream is = streamingEntity.getStringStreamed().deriveStringInputStream();
		try {
			int index = 0;
			while (is.hasString()) {
				String value = is.readString();
				Assert.assertEquals(expected[index++], value);
			}
		}
		finally {
			is.close();
		}
		Assert.assertNotNull(streamingEntity);
	}
}
