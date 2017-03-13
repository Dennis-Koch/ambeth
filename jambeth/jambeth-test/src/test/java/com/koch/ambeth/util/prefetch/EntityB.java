package com.koch.ambeth.util.prefetch;

import java.util.List;

public interface EntityB
{
	Integer getId();

	Integer getVersion();

	EntityB getParentBOfB();

	void setParentBOfB(EntityB parentB);

	List<EntityA> getAsOfB();

	List<EntityB> getBsOfB();

	List<EntityC> getCsOfB();
}
