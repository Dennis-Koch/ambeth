package com.koch.ambeth.cache.stream;

import java.io.BufferedInputStream;
import java.io.InputStream;

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
import com.koch.ambeth.util.collections.IList;

public abstract class AbstractInputSourceValueHolder implements IInputSource, IParentEntityAware, IInitializingBean
{
	protected int chunkSize = 65536;

	protected IServiceContext beanContext;

	protected String chunkProviderName;

	protected Member member;

	protected Object parentEntity;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "beanContext");
		ParamChecker.assertNotNull(chunkProviderName, "chunkProviderName");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setChunkProviderName(String chunkProviderName)
	{
		this.chunkProviderName = chunkProviderName;
	}

	public void setChunkSize(int chunkSize)
	{
		this.chunkSize = chunkSize;
	}

	@Override
	public void setParentEntity(Object parentEntity, Member member)
	{
		this.parentEntity = parentEntity;
		this.member = member;
	}

	public IObjRelation getSelf()
	{
		IObjRefHelper oriHelper = beanContext.getService(IObjRefHelper.class);
		IList<IObjRef> allObjRefs = oriHelper.entityToAllObjRefs(parentEntity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), member.getName());
	}

	protected IChunkProvider getChunkProvider()
	{
		return beanContext.getService(chunkProviderName, IChunkProvider.class);
	}

	protected InputStream createBinaryInputStream()
	{
		IChunkProvider chunkProvider = getChunkProvider();
		return new BufferedInputStream(new ChunkProviderStubInputStream(getSelf(), chunkProvider), chunkSize);
	}
}
