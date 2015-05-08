package de.osthus.ambeth.persistence.jdbc.compositeid.models;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.IPrimaryKeyProvider;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;

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
