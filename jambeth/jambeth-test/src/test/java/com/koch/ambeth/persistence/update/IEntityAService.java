package com.koch.ambeth.persistence.update;

import com.koch.ambeth.util.annotation.NoProxy;

public interface IEntityAService
{
	void save(EntityA entity);

	@NoProxy
	void removeAndReSetEntityD(EntityA entity);
}
