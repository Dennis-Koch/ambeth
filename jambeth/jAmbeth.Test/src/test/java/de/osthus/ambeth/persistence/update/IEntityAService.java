package de.osthus.ambeth.persistence.update;

import de.osthus.ambeth.annotation.NoProxy;

public interface IEntityAService
{
	void save(EntityA entity);

	@NoProxy
	void removeAndReSetEntityD(EntityA entity);
}
