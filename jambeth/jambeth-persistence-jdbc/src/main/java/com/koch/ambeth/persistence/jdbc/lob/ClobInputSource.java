package com.koch.ambeth.persistence.jdbc.lob;

import com.koch.ambeth.cache.IParentEntityAware;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputStream;

public class ClobInputSource implements ICharacterInputSource, IParentEntityAware, IUnmodifiedInputSource
{
	protected final ILobInputSourceController lobInputSourceController;

	protected Object parentEntity;

	protected Member member;

	public ClobInputSource(ILobInputSourceController lobInputSourceController)
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
	public ICharacterInputStream deriveCharacterInputStream()
	{
		return lobInputSourceController.deriveCharacterInputStream(parentEntity, member);
	}
}
