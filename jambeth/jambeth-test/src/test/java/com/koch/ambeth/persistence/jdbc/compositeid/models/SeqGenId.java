package com.koch.ambeth.persistence.jdbc.compositeid.models;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public class SeqGenId implements IPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Override
	public void acquireIds(ITableMetaData table, IList<IObjRef> idlessObjRefs)
	{
		for (int a = idlessObjRefs.size(); a-- > 0;)
		{
			IObjRef objRef = idlessObjRefs.get(a);
			objRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);

			CompositeIdEntity entity = (CompositeIdEntity) ((IDirectObjRef) objRef).getDirect();
			IEntityMetaData metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
			Object id = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), 42, entity.getAltId4() + entity.getAltId2());
			objRef.setId(id);
		}
	}
}
