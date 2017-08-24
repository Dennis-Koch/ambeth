package com.koch.ambeth.cache.valueholdercontainer;

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

import java.util.List;

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.model.IMethodDescription;

public class CacheRetrieverMock
		implements ICacheRetriever, IMergeService, ICacheService, IStartingBean {
	protected final HashMap<IObjRef, ILoadContainer> databaseMap = new HashMap<>();

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjectCopier objectCopier;

	protected Object reader;

	public void setReader(Object reader) {
		this.reader = reader;
	}

	@Override
	public void afterStarted() {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);

		LoadContainer lc = new LoadContainer();
		lc.setReference(new ObjRef(Material.class, 1, 1));
		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);

		lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = "Name1";

		databaseMap.put(lc.getReference(), lc);

		IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(MaterialType.class);
		LoadContainer lc2 = new LoadContainer();
		lc2.setReference(new ObjRef(MaterialType.class, 2, 1));
		lc2.setPrimitives(new Object[metaData2.getPrimitiveMembers().length]);
		lc2.setRelations(new IObjRef[metaData2.getRelationMembers().length][]);

		lc2.getPrimitives()[metaData2.getIndexByPrimitiveName("Name")] = "Name2";

		lc.getRelations()[metaData.getIndexByRelationName("Types")] =
				new IObjRef[] {lc2.getReference()};

		databaseMap.put(lc2.getReference(), lc2);
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
		List<ILoadContainer> result = new ArrayList<>(orisToLoad.size());
		synchronized (databaseMap) {
			for (IObjRef oriToLoad : orisToLoad) {
				ILoadContainer lc = databaseMap.get(oriToLoad);
				if (lc == null) {
					continue;
				}
				result.add(lc);
			}
			result = objectCopier.clone(result);
		}
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IOriCollection merge(ICUDResult cudResult, String[] causingUuids,
			IMethodDescription methodDescription) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
		throw new UnsupportedOperationException();
	}

	public Class<?> getTargetProviderType(Class<?> clientInterface) {
		throw new UnsupportedOperationException();
	}

	public Class<?> getSyncInterceptorType(Class<?> clientInterface) {
		throw new UnsupportedOperationException();
	}

	public String getServiceName(Class<?> clientInterface) {
		throw new UnsupportedOperationException();
	}

	public void postProcessTargetProviderBean(String targetProviderBeanName,
			IBeanContextFactory beanContextFactory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String createMetaDataDOT() {
		throw new UnsupportedOperationException();
	}
}
