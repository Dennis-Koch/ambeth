package com.koch.ambeth.persistence.jdbc.lob;

import com.koch.ambeth.cache.IParentEntityAware;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputSource;
import com.koch.ambeth.stream.binary.IBinaryInputStream;

public class BlobInputSource implements IBinaryInputSource, IParentEntityAware, IUnmodifiedInputSource
{
	protected final ILobInputSourceController lobInputSourceController;

	protected Object parentEntity;

	protected Member member;

	public BlobInputSource(ILobInputSourceController lobInputSourceController)
	{
		this.lobInputSourceController = lobInputSourceController;
	}

	@Override
	public void setParentEntity(Object parentEntity, Member member)
	{
		this.parentEntity = parentEntity;
		this.member = member;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		return lobInputSourceController.deriveInputStream(parentEntity, member);
	}

	@Override
	public IBinaryInputStream deriveBinaryInputStream()
	{
		return lobInputSourceController.deriveBinaryInputStream(parentEntity, member);
	}
}
