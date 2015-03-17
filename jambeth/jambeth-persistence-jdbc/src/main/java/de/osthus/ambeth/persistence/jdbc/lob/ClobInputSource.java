package de.osthus.ambeth.persistence.jdbc.lob;

import de.osthus.ambeth.cache.IParentEntityAware;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.IUnmodifiedInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputSource;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;

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
