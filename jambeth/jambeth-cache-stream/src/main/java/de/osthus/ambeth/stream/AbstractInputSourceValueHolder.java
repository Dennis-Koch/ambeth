package de.osthus.ambeth.stream;

import java.io.BufferedInputStream;
import java.io.InputStream;

import de.osthus.ambeth.cache.IParentEntityAware;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.transfer.ObjRelation;
import de.osthus.ambeth.chunk.ChunkProviderStubInputStream;
import de.osthus.ambeth.chunk.IChunkProvider;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.ParamChecker;

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
