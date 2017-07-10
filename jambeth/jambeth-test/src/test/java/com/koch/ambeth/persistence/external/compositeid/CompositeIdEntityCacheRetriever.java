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

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;

public class CompositeIdEntityCacheRetriever implements ICacheRetriever, IInitializingBean {
	public static final Object[] id1_2_data = { (int) 1, "einszwo", "name_einszwo1", (short) 2,
			(long) 3 };

	public static final Object[] entity2_id1_2_data = { (int) 1, "einszwo", "name_einszwo1",
			(short) 2, "alt_einszwo" };

	protected ICompositeIdFactory compositeIdFactory;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(compositeIdFactory, "compositeIdFactory");
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
	}

	public void setCompositeIdFactory(ICompositeIdFactory compositeIdFactory) {
		this.compositeIdFactory = compositeIdFactory;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider) {
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
		ArrayList<ILoadContainer> lcs = new ArrayList<>(orisToLoad.size());
		for (int a = 0, size = orisToLoad.size(); a < size; a++) {
			IObjRef objRef = orisToLoad.get(a);
			Class<?> realType = objRef.getRealType();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(realType);
			if (CompositeIdEntity.class.equals(realType)) {
				handleCompositeIdEntity(metaData, lcs, objRef);
			}
			else if (CompositeIdEntity2.class.equals(realType)) {
				handleCompositeIdEntity2(metaData, lcs, objRef);
			}
		}
		return lcs;
	}

	protected void handleCompositeIdEntity(IEntityMetaData metaData, Collection<ILoadContainer> lcs,
			IObjRef objRef) {
		Object id = objRef.getId();
		byte idNameIndex = objRef.getIdNameIndex();
		if (idNameIndex == ObjRef.PRIMARY_KEY_INDEX) {
			Object id1 = ((CompositeIdMember) metaData.getIdMember()).getDecompositedValue(id, 0);
			Object id2 = ((CompositeIdMember) metaData.getIdMember()).getDecompositedValue(id, 1);
			if (id1_2_data[0].equals(id1) && id1_2_data[1].equals(id2)) {
				LoadContainer lc = new LoadContainer();
				lc.setReference(objRef);
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2")] = id1_2_data[4];
				lcs.add(lc);
			}
		}
		else if (idNameIndex == 0) {
			Object id1 = ((CompositeIdMember) metaData.getAlternateIdMembers()[idNameIndex])
					.getDecompositedValue(id, 1);
			Object id2 = ((CompositeIdMember) metaData.getAlternateIdMembers()[idNameIndex])
					.getDecompositedValue(id, 0);
			if (id1_2_data[3].equals(id1) && id1_2_data[4].equals(id2)) {
				LoadContainer lc = new LoadContainer();
				Object primaryId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(),
						id1_2_data[0], id1_2_data[1]);
				lc.setReference(
						new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, primaryId, null));
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2")] = id1_2_data[4];
				lcs.add(lc);
			}
		}
	}

	protected void handleCompositeIdEntity2(IEntityMetaData metaData, Collection<ILoadContainer> lcs,
			IObjRef objRef) {
		Object id = objRef.getId();
		byte idNameIndex = objRef.getIdNameIndex();
		if (idNameIndex == ObjRef.PRIMARY_KEY_INDEX) {
			Object id1 = ((CompositeIdMember) metaData.getIdMember()).getDecompositedValue(id, 0);
			Object id2 = ((CompositeIdMember) metaData.getIdMember()).getDecompositedValue(id, 1);
			if (entity2_id1_2_data[0].equals(id1) && entity2_id1_2_data[1].equals(id2)) {
				LoadContainer lc = new LoadContainer();
				lc.setReference(objRef);
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = entity2_id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = entity2_id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2.Sid")] = entity2_id1_2_data[4];
				lcs.add(lc);
			}
		}
		else if (idNameIndex == 0) {
			Object id1 = ((CompositeIdMember) metaData.getAlternateIdMembers()[idNameIndex])
					.getDecompositedValue(id, 1);
			Object id2 = ((CompositeIdMember) metaData.getAlternateIdMembers()[idNameIndex])
					.getDecompositedValue(id, 0);
			if (entity2_id1_2_data[3].equals(id1) && entity2_id1_2_data[4].equals(id2)) {
				LoadContainer lc = new LoadContainer();
				Object primaryId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(),
						entity2_id1_2_data[0], entity2_id1_2_data[1]);
				lc.setReference(
						new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, primaryId, null));
				lc.setRelations(ObjRef.EMPTY_ARRAY_ARRAY);
				lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = entity2_id1_2_data[2];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid1")] = entity2_id1_2_data[3];
				lc.getPrimitives()[metaData.getIndexByPrimitiveName("Aid2.Sid")] = entity2_id1_2_data[4];
				lcs.add(lc);
			}
		}
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		throw new UnsupportedOperationException();
	}
}
