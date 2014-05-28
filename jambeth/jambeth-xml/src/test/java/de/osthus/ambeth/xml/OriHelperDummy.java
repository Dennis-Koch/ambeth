package de.osthus.ambeth.xml;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.EntityCallback;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IObjRefProvider;
import de.osthus.ambeth.merge.MergeHandle;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public class OriHelperDummy implements IObjRefHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle)
	{
		return null;
	}

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList)
	{
		return null;
	}

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList, EntityCallback entityCallback)
	{
		return null;
	}

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, IObjRefProvider oriProvider, IList<IObjRef> targetOriList, EntityCallback entityCallback)
	{
		return null;
	}

	@Override
	public IObjRef getCreateObjRef(Object obj, IObjRefProvider oriProvider)
	{
		return null;
	}

	@Override
	public IObjRef getCreateObjRef(Object obj, MergeHandle mergeHandle)
	{
		return null;
	}

	@Override
	public IObjRef entityToObjRef(Object entity)
	{
		return null;
	}

	@Override
	public IObjRef entityToObjRef(Object entity, byte idIndex)
	{
		return null;
	}

	@Override
	public IObjRef entityToObjRef(Object entity, IEntityMetaData metaData)
	{
		return null;
	}

	@Override
	public IObjRef entityToObjRef(Object entity, byte idIndex, IEntityMetaData metaData)
	{
		return null;
	}

	@Override
	public IList<IObjRef> entityToAllObjRefs(Object id, Object version, Object[] primitives, IEntityMetaData metaData)
	{
		return null;
	}

	@Override
	public IList<IObjRef> entityToAllObjRefs(Object entity)
	{
		return null;
	}

	@Override
	public IList<IObjRef> entityToAllObjRefs(Object entity, IEntityMetaData metaData)
	{
		return null;
	}

	@Override
	public IObjRef entityToObjRef(Object entity, boolean forceOri)
	{
		return null;
	}

	@Override
	public IObjRef entityToObjRef(Object entity, byte idIndex, IEntityMetaData metaData, boolean forceOri)
	{
		return null;
	}
}
