package com.koch.ambeth.persistence.jdbc.compositeid.models;

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
