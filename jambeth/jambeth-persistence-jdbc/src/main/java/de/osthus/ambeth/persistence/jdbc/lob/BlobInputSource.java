package de.osthus.ambeth.persistence.jdbc.lob;

import de.osthus.ambeth.cache.IParentEntityAware;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.IUnmodifiedInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputSource;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;

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
