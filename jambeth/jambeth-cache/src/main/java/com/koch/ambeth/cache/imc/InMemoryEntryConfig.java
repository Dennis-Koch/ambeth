package com.koch.ambeth.cache.imc;

import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.HashSet;

public class InMemoryEntryConfig implements IInMemoryConfig
{
	protected final IEntityMetaData metaData;

	protected final LoadContainer lc;

	protected final InMemoryCacheRetriever inMemoryCacheRetriever;

	public InMemoryEntryConfig(InMemoryCacheRetriever inMemoryCacheRetriever, IEntityMetaData metaData, LoadContainer lc)
	{
		this.inMemoryCacheRetriever = inMemoryCacheRetriever;
		this.metaData = metaData;
		this.lc = lc;
	}

	@Override
	public IInMemoryConfig primitive(String memberName, Object value)
	{
		int primitiveIndex = metaData.getIndexByPrimitiveName(memberName);
		lc.getPrimitives()[primitiveIndex] = value;

		if (metaData.isAlternateId(metaData.getMemberByName(memberName)))
		{
			inMemoryCacheRetriever.addWithKey(lc, memberName, value);
		}
		return this;
	}

	@Override
	public IInMemoryConfig relation(String memberName, IObjRef... objRefs)
	{
		int relationIndex = metaData.getIndexByRelationName(memberName);
		lc.getRelations()[relationIndex] = objRefs;
		return this;
	}

	@Override
	public IInMemoryConfig relation(String memberName, IInMemoryConfig... inMemoryConfigs)
	{
		IObjRef[] objRefs = new IObjRef[inMemoryConfigs.length];
		for (int a = inMemoryConfigs.length; a-- > 0;)
		{
			objRefs[a] = ((InMemoryEntryConfig) inMemoryConfigs[a]).lc.getReference();
		}
		return relation(memberName, objRefs);
	}

	@Override
	public IInMemoryConfig addRelation(String memberName, IObjRef... objRefs)
	{
		int relationIndex = metaData.getIndexByRelationName(memberName);
		IObjRef[] existingObjRefs = lc.getRelations()[relationIndex];
		HashSet<IObjRef> existingObjRefsSet = HashSet.create((existingObjRefs != null ? existingObjRefs.length : 0) + objRefs.length);
		if (existingObjRefs != null)
		{
			existingObjRefsSet.addAll(existingObjRefs);
		}
		existingObjRefsSet.addAll(objRefs);
		lc.getRelations()[relationIndex] = existingObjRefsSet.toArray(IObjRef.class);
		return this;
	}

	@Override
	public IInMemoryConfig addRelation(String memberName, IInMemoryConfig... inMemoryConfigs)
	{
		IObjRef[] objRefs = new IObjRef[inMemoryConfigs.length];
		for (int a = inMemoryConfigs.length; a-- > 0;)
		{
			objRefs[a] = ((InMemoryEntryConfig) inMemoryConfigs[a]).lc.getReference();
		}
		return addRelation(memberName, objRefs);
	}
}