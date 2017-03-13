package com.koch.ambeth.util.prefetch;

import java.util.List;

public interface EntityC
{
	Integer getId();

	Integer getVersion();

	EntityC getParentCOfC();

	void setParentCOfC(EntityC parentC);

	List<EntityC> getCsOfC();
}
