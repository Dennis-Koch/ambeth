package de.osthus.ambeth.service;

import de.osthus.ambeth.persistence.IDirectedLink;

public class EntityLinkKey
{
	protected final Object entity;

	protected final IDirectedLink link;

	public EntityLinkKey(Object entity, IDirectedLink link)
	{
		this.entity = entity;
		this.link = link;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof EntityLinkKey))
		{
			return false;
		}
		EntityLinkKey other = (EntityLinkKey) obj;
		return entity.equals(other.entity) && link.equals(other.link);
	}

	@Override
	public int hashCode()
	{
		return entity.hashCode() ^ link.hashCode();
	}

	@Override
	public String toString()
	{
		return entity + " - " + link.getName();
	}
}
