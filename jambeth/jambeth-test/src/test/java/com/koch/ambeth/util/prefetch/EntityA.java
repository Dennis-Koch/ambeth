package com.koch.ambeth.util.prefetch;

import java.util.List;

public interface EntityA
{
	Integer getId();

	Integer getVersion();

	EntityA getParentAOfA();

	void setParentAOfA(EntityA parentA);

	List<EntityA> getAsOfA();

	List<EntityB> getBsOfA();

	List<EntityC> getCsOfA();
}
